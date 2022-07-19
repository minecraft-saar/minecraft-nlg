<template>
  <div class="q-gutter-y-md column ">
    <q-toolbar class="shadow-2 rounded-borders bg-black text-white">
    <q-select
      id="senselect"
      filled
      :options="logs"
      :label="nameRepr ? 'sentence' : 'derivation'"
      :display-value="nameRepr ? selectedAlt : trimtilde(selected)"  
      color="teal"
      label-color="teal"
      v-model="selected"
      :input-class="{ 'text-red': true }"
    >
      <!--- from: https://github.com/quasarframework/quasar/issues/5724:--->
      <template v-slot:option="scope">
        <q-expansion-item
          expand-separator
          group="somegroup"
          header-class="text-weight-bold"
          :label="'timestamp '+scope.index"
        >
          <template v-for="child in scope.opt" :key="child.label">
            <q-item
              clickable
              v-ripple
              v-close-popup
              @click="() => {selected = child[0]; selectedAlt = child[1];}"
              :class="{ 'bg-teal-1': selected == child[0] }"
            >
              <q-item-section>
                <q-item-label class="q-ml-md" >{{ nameRepr ? child[1] : trimtilde(child[0]) }}</q-item-label>
              </q-item-section>
            </q-item>
          </template>
        </q-expansion-item>
      </template>
    </q-select>
    <q-space />
    <q-toggle
        :label="`display ${nameRepr ? '(' : ''}derivation${nameRepr ? ')' : ''} or ${nameRepr ? '' : '('}sentence${nameRepr ? '' : ')'} `"
        color="teal"
        v-model="nameRepr"
        class="text-weight-medium"
      />
    </q-toolbar>  
  </div>
  <br/>
  <q-card flat class="row justify-center">
  <McWorld style="border: 3px solid rgba(0, 0, 0, 0.12);" :blockList="blocks"/>
  </q-card>
  <br/>
  <div class="row justify-center">
  <q-list style="width:85vw;" padding bordered class="rounded-borders">
    <q-expansion-item
      dense
      dense-toggle
      expand-separator
      icon="polyline"
      label="block positions"
    >
      <q-card>
        <q-card-section>
          {{filterColor(blocks)}}
        </q-card-section>
      </q-card>
    </q-expansion-item>
    <q-expansion-item
      dense
      dense-toggle
      expand-separator
      icon="view_in_ar"
      label="model"
    >
      <q-card>
        <q-card-section>
          {{model}}
        </q-card-section>
      </q-card>
    </q-expansion-item>
  </q-list>
  </div>
  <br/>

  <q-card class="row justify-center" flat>
  <q-card style="width:65vw;border: 1px solid rgba(0, 0, 0, 0.12);" :flat="false">
  <div class="tooltip-wrapper">
  <v-network-graph
    ref="graph"
    :nodes="nodes"
    :edges="edges"
    :configs="configs"
    :layouts="layouts"
    :event-handlers="eventHandlers"
  />
  <!-- Tooltip -->
  <div
    ref="tooltip"
    class="tooltip"
    :style="{ ...tooltipPos, opacity: tooltipOpacity, 'z-index': tooltipZ }"
    @mouseover="eventHandlers['node:pointerover']({'node':null})"
    @mouseout="eventHandlers['node:pointerout']()"
  >
    <div>{{ targetNodeId != "" ? getRule(targetNodeId) : "" }} <br/><br/> {{ targetNodeId != "" ? getString(nodes, edges, targetNodeId) : "" }}</div>
  </div>
  </div>
  </q-card>
  </q-card>
</template>

<script setup>
import McWorld from './McWorld.vue'
import edges_json from '../data/edges.json'
import nodes_json from '../data/nodes.json'
import names_json from '../data/names.json'
import blocks_json from '../data/blocks.json'
import models_json from '../data/models.json'
import rules from '../data/rules.json'
import * as vNG from "v-network-graph"
import { ref, onMounted, computed, reactive, watch } from 'vue'
import dagre from 'dagre'

const getLinkedNode = (edges, nodeId, i) => {
  var j = 0;
  for (var key in edges) {
    if (edges[key].source == nodeId) {
      if (i == j) {
        return edges[key].target;
      }
      j++;
    }
  }
}

const getString = (nodes, edges, nodeId) => {
  var text = "";
  var ruleName = nodes[nodeId].name;
  var rule = rules[ruleName];
  for (var part of rule) {
    if (part.startsWith("?")) {
      var i = parseInt(part.substring(1));
      text += getString(nodes, edges, getLinkedNode(edges, nodeId, i));
    } else {
      text += part;
    }
    text += ' ';
  }
  return text;
}

var nameRepr = ref(false);
const backwardsMap = new Map();

const log_num = Object.keys(names_json).length;
var logs = [];
for (var i = 0; i < log_num; i++) {
  logs.push(names_json[i.toString()].map(s => [s, getString(nodes_json[s], edges_json[s], Object.keys(nodes_json[s])[0])]));
  for (var s of logs[i]) {
    backwardsMap.set(s[0], i.toString());
  }
}

const filterColor = (arr) => arr.map(obj => {return {'x': obj.x, 'y': obj.y, 'z': obj.z}});

const configs = vNG.defineConfigs({
  node: {
    selectable: true,
    normal: {
      radius: 16,
      type: "rect",
      color: "#ffffff",
      strokeWidth: 0,
      strokeColor: "#000000",
    },
    hover: {
      color: "#f0f0f0",
    },
    label: {
      fontSize: 12,
      color: "#000000",
      direction: "center",
    },
  },
  edge: {
    normal: {
      color: "#aaa",
      width: 3,
    },
    margin: 4,
    marker: {
      target: {
        type: "arrow",
        width: 4,
        height: 4,
      },
    },
  }
});

const tooltip = ref()
const NODE_RADIUS = 16
const targetNodeId = ref("")

const tooltipPos = computed(() => {
  // from: https://dash14.github.io/v-network-graph/examples/event.html#tooltip
  if (!graph.value || !tooltip.value) return { x: 0, y: 0 }
  if (!targetNodeId.value) return { x: 0, y: 0 }

  const nodePos = layouts.nodes[targetNodeId.value]
  // translate coordinates: SVG -> DOM
  const domPoint = graph.value.translateFromSvgToDomCoordinates(nodePos)
  // calculates top-left position of the tooltip.
  return {
    left: domPoint.x - tooltip.value.offsetWidth / 2 + "px",
    top: domPoint.y - NODE_RADIUS - tooltip.value.offsetHeight - 10 + "px",
  }
})

let tooltipchange = 0;
const tooltipOpacity = ref(0)
const tooltipZ = computed(() => {
  if (!graph.value || !tooltip.value) return -999
  if (!targetNodeId.value || tooltipOpacity.value == 0) return -999

  return 999;
})

const eventHandlers = {
  "node:pointerover": ({ node }) => {
    if (node) {
      targetNodeId.value = node;
    }
    tooltipOpacity.value = 1; // show
    tooltipchange = Date.now();
  },
  "node:pointerout": () => {
    setTimeout(((now) => () => {
      if (tooltipchange <= now) {
        tooltipOpacity.value = 0; // hide
      }
    })(Date.now()) , 200);
  },
}

const getRule = (nodeId) => {
  var text = "";
  var ruleName = nodes.value[nodeId].name;
  var rule = rules[ruleName];
  text += ruleName + ' → ' + rule.join(', ');
  return text;
}

const trimtilde = (s) => s.substring(0, s.indexOf("~"));

var _selected = ref(names_json["0"][0]);
const selected = computed({
  get () {
    return _selected.value;
  },

  set (value) {
    _selected.value = value;
    return value;
  }
});
const selectedAlt = ref(ref(names_json["0"][1]));

watch(_selected, () => {
  nodes.value = nodes_json[_selected.value];
  edges.value = edges_json[_selected.value];
  blocks.value = blocks_json[backwardsMap.get(_selected.value)]
  model.value = JSON.stringify(models_json[backwardsMap.get(_selected.value)])
  targetNodeId.value = "";
  layout("TB");
});

const nodes = ref(nodes_json[_selected.value]);
const edges = ref(edges_json[_selected.value]);
const blocks = ref(blocks_json["0"]);
const model = ref(JSON.stringify(models_json["0"]));
const nodeSize = 40;
const graph = ref();
const layouts = reactive({
  nodes: {},
});

onMounted(() => {
  layout("TB");
});

function layout(direction) {
  direction = "TB";

  // from: https://dash14.github.io/v-network-graph/examples/layout.html#automatic-layout
  if (Object.keys(nodes).length <= 1 || Object.keys(edges).length == 0) {
    return
  }

  // convert graph
  // ref: https://github.com/dagrejs/dagre/wiki
  const g = new dagre.graphlib.Graph()
  // Set an object for the graph label
  g.setGraph({
    rankdir: direction,
    nodesep: nodeSize * 2,
    edgesep: nodeSize,
    ranksep: nodeSize * 2,
  })
  // Default to assigning a new object as a label for each new edge.
  g.setDefaultEdgeLabel(() => ({}))

  // Add nodes to the graph. The first argument is the node id. The second is
  // metadata about the node. In this case we're going to add labels to each of
  // our nodes.
  Object.entries(nodes.value).forEach(([nodeId, node]) => {
    g.setNode(nodeId, { label: node.name, width: nodeSize, height: nodeSize })
  });

  // Add edges to the graph.
  Object.values(edges.value).forEach(edge => {
    g.setEdge(edge.source, edge.target)
  });

  dagre.layout(g)

  const box = {}
  g.nodes().forEach((nodeId) => {
    if (nodeId == "undefined") {
      return;
    }
    // update node position
    const x = g.node(nodeId).x
    const y = g.node(nodeId).y
    layouts.nodes[nodeId] = { x, y }

    // calculate bounding box size
    box.top = box.top ? Math.min(box.top, y) : y
    box.bottom = box.bottom ? Math.max(box.bottom, y) : y
    box.left = box.left ? Math.min(box.left, x) : x
    box.right = box.right ? Math.max(box.right, x) : x
  })

  const graphMargin = nodeSize * 2
  const viewBox = {
    top: (box.top ?? 0) - graphMargin,
    bottom: (box.bottom ?? 0) + graphMargin,
    left: (box.left ?? 0) - graphMargin,
    right: (box.right ?? 0) + graphMargin,
  }
  graph.value?.setViewBox(viewBox)
}

</script>

<style lang="css" scoped>
.tooltip-wrapper {
  position: relative;
}
.tooltip {
  top: 0;
  left: 0;
  opacity: 0;
  position: absolute;
  width: 200px;
  padding: 10px;
  text-align: center;
  font-size: 12px;
  background-color: rgb(224, 224, 224);
  border: 1px solid rgba(0, 0, 0, 0.018);
  box-shadow: 2px 2px 2px rgba(0, 0, 0, 0.45);
  transition: opacity 0.2s linear;
}
</style>
<style>
#senselect > span {
  color: white !important;
}
</style>
<style src="@vueform/toggle/themes/default.css"></style>