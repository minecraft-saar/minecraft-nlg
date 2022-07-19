package de.saar.coli.minecraft;

import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Map;

public class IRTGTransformer {
    
    private static class StaticResolveInfo {
        public ArrayList<String> newLines;
        public String[] relevantLines;
        public String[] metaKeys;
        public Map<String, Object> metaKeyMap;

        public StaticResolveInfo(ArrayList<String> newLines, String[] relevantLines, String[] metaKeys, Map<String, Object> metaKeyMap) {
            this.newLines = newLines;
            this.relevantLines = relevantLines;
            this.metaKeys = metaKeys;
            this.metaKeyMap = metaKeyMap;
        }
    }

    private static void addResolvedRules(StaticResolveInfo info) {
        addResolvedRules(info, 0, new ArrayList<>());
    }

    private static void addResolvedRules(StaticResolveInfo info, int metaKeyIndex, ArrayList<String> combination) { //TODO: rather array than list?
        if (info.metaKeys.length == metaKeyIndex) {
            Logger.debug("Meta combination resolved: " + combination);
            for (String s : info.relevantLines) {
            for (int i = 0; i < combination.size(); i++) {
                s = s.replace("@" + (i+1), combination.get(i));
            }
            Logger.debug("Created new line: " + s);
            info.newLines.add(s);
            }
            info.newLines.add("\n");
        } else {
            String currentValues = info.metaKeyMap.get(info.metaKeys[metaKeyIndex]).toString();
            
            for (String tuple : currentValues.split(";")) {
            assert tuple.charAt(0) == '(';
            assert tuple.charAt(tuple.length()-1) == ')';

            ArrayList<String> newCombination = new ArrayList<>();
            newCombination.addAll(combination);

            for (String val : tuple.substring(1, tuple.length()-1).split(",")) {
                newCombination.add(val.strip());
            }
            addResolvedRules(info, metaKeyIndex+1, newCombination);
            }
        }
    }

    private static String convertMetaDirectives(String[] lines) {
        ArrayList<String> newLines = new ArrayList<>();
        Yaml yaml = new Yaml();
        InputStream inputStream = MinecraftRealizer.class.getResourceAsStream("metaKeys.yml");
        Map<String, Object> metaKeyMap = yaml.load(inputStream);
        Logger.debug("Meta key map loaded: " + metaKeyMap);

        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].strip();
            if (lines[i].startsWith("@Meta")) {
            Logger.debug("Found Meta line in irtg file: " + lines[i]);
            assert lines.length > i + 4;
            assert lines[i+2].startsWith("[string]");
            assert lines[i+3].startsWith("[ref]");
            assert lines[i+4].startsWith("[sem]");
            String[] relevantLines = {lines[i+1], lines[i+2], lines[i+3], lines[i+4]};

            String metaLine = lines[i].substring("@Meta".length());
            assert metaLine.charAt(0) == '[';
            assert metaLine.charAt(metaLine.length()-1) == ']';
            String metaArgument = metaLine.substring(1, metaLine.length()-1);

            String[] metaKeys = metaArgument.split("\\*");
            Logger.debug("Meta keys resolved: " + String.join(", ", metaKeys));
            addResolvedRules(new StaticResolveInfo(newLines, relevantLines, metaKeys, metaKeyMap));

            i += 4;
            } else {
            newLines.add(lines[i]);
            }
        }

        return String.join("\n", newLines);
    }

    public static String simplify(String[] lines) {
        return convertMetaDirectives(lines);
    }
}
