/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author srivid
 */
public class trie {
    trieNode root;

    public trieNode getRoot() {
        return root;
    }
    
    public trie() {
        root = new trieNode('#');
    }
    
    public trieNode searchName(String name) {
        trieNode ptr = root;
        for (int i = 0, max = name.length(); i < max; i++) {
            boolean found = false;
            for (int j = 0, maxj = ptr.getChildCount(); j < maxj; j++) {
                if(ptr.getChild(j).getAlphabet() == name.charAt(i)) {
                    found = true;
                    ptr = ptr.getChild(j);
                    break;
                }
            }
            if (!found) {
                if (i != 0) {
                    return ptr;
                } else {
                    return null;
                }
            }
        }
        return ptr;
    }
    
    public trieNode addName(String name) {
        trieNode temp = root;
        for (int i = 0, max = name.length(); i < max; i++) {
            int childCount = temp.getChildCount();
            boolean found = false;
            for (int j = 0; j < childCount; j++) {
                trieNode child = temp.getChild(j);
                if (child != null && child.getAlphabet() == name.charAt(i)) {
                    found = true;
                    temp = child;
                    break;
                }
            }
            if (!found) {
                temp.addChild(new trieNode(name.charAt(i)));
                temp = temp.getChild(temp.getChildCount() - 1);
            }
        }
        return temp;
    }
    
    public trieNode addName(String name, Object endObject) {
        trieNode t = this.addName(name);
        if(t != null) {
            t.setEnd(endObject);
        }
        return t;
    }
    
    public void addNames(ArrayList<String> names) {
        for(String name:names) {
            this.addName(name);
        }
    }
    
    public void addNames(HashMap<String,Object> nameToObj) {
        for(Map.Entry<String,Object> entry : nameToObj.entrySet()) {
            this.addName(entry.getKey(), entry.getValue());
        }
    }
    
    public HashMap<String,Object> autocomplete(String prefix, trieNode from) {
        ArrayList<trieNode> stack = new ArrayList<trieNode>();
        stack.add(from);
        String suffix = "";
        HashMap<String,Object> hash = new HashMap<String, Object>();
        while (stack.size() != 0) {
            trieNode curNode = (stack.get(stack.size() - 1)).getNextChild();
            if (curNode != null) {
                suffix += curNode.getAlphabet();
                if(curNode.isEnd()) {
                    hash.put(prefix + suffix, curNode.getEndObject());
                }
                stack.add(curNode);
            } else {
                if (suffix.length() > 0) {
                    suffix = suffix.substring(0, suffix.length() - 1);
                }
                //this resets the childpointer so that the entire trie is reusable after one pass
                stack.get(stack.size() - 1).resetChildPtr();
                stack.remove(stack.size() - 1);
            }
        }
        return hash;
    }
    
    /*
     * API to parse a trie - does a DFS and prints out the contents of the trie.
     * This is only for test 
     */
    public void parse() {
        ArrayList<trieNode> stack = new ArrayList<trieNode>();
        stack.add(root);
        String name = "";
        while (stack.size() != 0) {
            trieNode curNode = (stack.get(stack.size() - 1)).getNextChild();
            if (curNode != null) {
                name += curNode.getAlphabet();
                if (curNode.isEnd()) {
                    System.out.println(name);
                }
                stack.add(curNode);
            } else {
                if (name.length() > 0) {
                    name = name.substring(0, name.length() - 1);
                }
                stack.get(stack.size() - 1).resetChildPtr();
                stack.remove(stack.size() - 1);
            }
        }
    }
}
