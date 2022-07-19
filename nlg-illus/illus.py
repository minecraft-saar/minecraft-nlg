import sys
import re
import os
import subprocess
import json

if len(sys.argv) != 3:
    print("please provide exactly 2 arguments <path to log> <simplified.irtg>")
    exit(1)

indent = lambda s: 4*' ' + s
logs = []
file_loc = os.path.dirname(__file__)

COLORS = ["blue_wool", "stone"]

data_dir = file_loc + os.sep + 'vue-graph' + os.sep + 'src' + os.sep + 'data' + os.sep
wrap_data_dir = lambda s: data_dir + s

class IntWrapper:
    def __init__(self, i):
        self.i = i
    
    def next(self):
        self.i += 1
        return self.i

class Node:
    def __init__(self, s, children):
        self.id = -1
        self.s = s
        self.children = children

    def tree_repr(self, indent=0):
        s = ' '*indent + self.s + '\n'
        for c in self.children:
            s += c.tree_repr(indent+1)
        return s

    def simple_repr(self):
        return '%s(%s)' % (self.s, ','.join(c.simple_repr() for c in self.children))

    def simple_repr_id(self):
        return self.simple_repr() + '~' + str(self.id)

    def node_repr(self):
        return '"node%d": { "name": "%s" }' % (self.id, self.s)

    def edge_repr(self, edge_count):
        return ',\n'.join('"edge%d": { "source": "node%d", "target": "node%d" }' % (edge_count.next(), self.id, c.id) for c in self.children)

    def get_all_node_repr(self):
        return [self.node_repr()] + [node_repr for c in self.children for node_repr in c.get_all_node_repr()]

    def get_all_edge_repr(self, edge_count): 
        return [self.edge_repr(edge_count)] + [edge_repr for c in self.children for edge_repr in c.get_all_edge_repr(edge_count)]

    def collect_node_repr(self):
        return '{\n' + ",\n".join(map(indent, self.get_all_node_repr())) + '\n}'

    def collect_edge_repr(self): 
        edge_count = IntWrapper(-1)
        all_edges =  ",\n".join(s for s in self.get_all_edge_repr(edge_count) if s != '')
        indented_edges = '\n'.join(map(indent, all_edges.split('\n')))
        return '{\n' + indented_edges + '\n}'

    def __str__(self):
        return '--- %s ---\n' % self.simple_repr()\
            +  self.tree_repr()\
            +  '------'

    def set_id(self, wrapper):
        self.id = wrapper.i
        wrapper.i += 1
        for c in self.children:
            c.set_id(wrapper)

class Leaf(Node):
    def __init__(self, s):
        Node.__init__(self, s, [])


def comma_split(s):
    # only split at commas outside parens
    li = []
    before = 0
    par_count = 0
    for i, c in enumerate(s):
        if par_count == 0 and before != 0:
            li.append(i)
        before = par_count
        if c == '(':
            par_count += 1
        if c == ')':
            par_count -= 1

    strings = []
    for pos in li:
        strings.append(s[:pos])
        s = s[pos+1:]
    strings.append(s)
    return strings

def extract(s):
    if '(' not in s:
        return s
    first_paren = s.find('(')
    assert s[-1] == ')'
    return s[:first_paren], list(map(extract, comma_split(s[first_paren+1:-1])))

def parse_tree(pyrepr):
    if type(pyrepr) is not tuple:
        assert type(pyrepr) is str
        return Leaf(pyrepr)

    return Node(pyrepr[0], list(map(parse_tree, pyrepr[1])))

def prune_one_line_comments(s):
    return re.sub(r'//[^\n]*', '', s)

def prune_multi_line_comments(s):
    return re.sub(r'/\*.*?\*/', '', s, flags=re.S)

def prune_comments(s):
    s = prune_one_line_comments(s)
    s = prune_multi_line_comments(s)
    return s

with open(sys.argv[2]) as f:
    text = f.read()
    text = prune_comments(text)
    split_text = text.split("\n")
    indices = [i for i, l in enumerate(split_text) if "->" in l]
    rule_mapping = dict()
    for i in indices:
        line = split_text[i]
        rule_name = line[line.find("->")+2:(line.find("(") if line.find("(") != -1 else line.find("["))].strip()
        rule = split_text[i+1].replace('[string]', '').replace('*', '').replace('(', '').replace(')', '')
        rule = rule.split(",")
        rule = map(lambda s: s.strip(), rule)
        rule = map(lambda s: "?"+str(int(s[1:])-1) if s[0] == "?" else s[1:-1], rule)
        rule = list(rule)
        rule_mapping[rule_name] = rule
    with open(wrap_data_dir('rules.json'), 'w') as f:
        print(json.dumps(rule_mapping), file=f)

model_DEBUG_s = "DEBUG: generating a statement for this model:"
all_blocks = []
models = []

with open(sys.argv[1]) as f:
    text = f.read()
    matches = re.findall(r'===== Debug Output: all trees =====([^=]*)==========', text, re.MULTILINE|re.DOTALL)
    for match in matches:
        trees = [] #(rule chains)
        lines = [s for s in match.split() if not s.isspace()]
        for line in lines:
            pyrepr = extract(line)
            tree = parse_tree(pyrepr)
            trees.append(tree)
        logs.append(trees)
    for s in text.split("\n"):
        if model_DEBUG_s in s:
            model = eval(s[len(model_DEBUG_s):])
            models.append(model)
            color_map = dict((entry[0], color) for color in COLORS for entry in model[color])
            model = model['block']
            blocks = []
            for b in model:
                s = b[0]
                b = b[0][len('Block-'):]
                if not b.startswith('In'):
                    x = int(b[:b.find("-")])
                    b = b[b.find("-")+1:]
                    y = int(b[:b.find("-")])
                    b = b[b.find("-")+1:]
                    z = int(b)
                    color = 'none'
                    if s in color_map:
                        color = color_map[s]
                    blocks.append({'x': x, 'y': y, 'z': z, 'color': color})
            all_blocks.append(blocks)

with open(wrap_data_dir('blocks.json'), 'w') as f:
    print(json.dumps(dict((str(i),block) for i, block in enumerate(all_blocks))), file=f)
    
with open(wrap_data_dir('models.json'), 'w') as f:
    print(json.dumps(dict((str(i),model) for i, model in enumerate(models))), file=f)

next_id = IntWrapper(0)
for trees in logs:
    for tree in trees:
        tree.set_id(next_id)

node_dict = ",\n".join('"%s": %s' % (tree.simple_repr_id(), tree.collect_node_repr()) for trees in logs for tree in trees)
#indent and wrap in parens
node_dict = '{\n' + '\n'.join(map(indent, node_dict.split('\n'))) + '\n}'

with open(wrap_data_dir('nodes.json'), 'w') as f:
    print(node_dict, file=f)

edge_dict = ",\n".join('"%s": %s' % (tree.simple_repr_id(), tree.collect_edge_repr()) for trees in logs for tree in trees)
edge_dict = '{\n' + '\n'.join(map(indent, edge_dict.split('\n'))) + '\n}'

with open(wrap_data_dir('edges.json'), 'w') as f:
    print(edge_dict, file=f)

def listify(li):
    return "[\n" + ",\n".join(map(indent, map(indent, map(lambda t: '"%s"' % t.simple_repr_id(), li)))) + "\n]"

name_dict = ",\n".join('"%d": %s' % (i, listify(trees)) for i, trees in enumerate(logs))
name_dict = '{\n' + '\n'.join(map(indent, name_dict.split('\n'))) + '\n}'

with open(wrap_data_dir('names.json'), 'w') as f:
    print(name_dict, file=f)
