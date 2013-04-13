/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb;

import java.util.ArrayList;

/**
 *
 * @author srivid
 */
public class trieNode<T> {

    private char alphabet;
    private ArrayList<trieNode> children;
    private boolean end;
    private int childPointer = 0;
    //this is to track multiple objects that a searched word leads to
    private ArrayList<T> endObjects;

    public int getChildCount() {
        return this.children.size();
    }

    public char getAlphabet() {
        return alphabet;
    }

    public boolean isEnd() {
        return end;
    }

    public ArrayList<T> getEndObject() {
        return this.endObjects;
    }

    public trieNode(char c) {
        this.alphabet = c;
        this.children = new ArrayList<trieNode>();
    }

    public void setEnd() {
        this.end = true;
    }

    public void setEnd(T o) {
        this.end = true;
        if(this.endObjects == null) {
            this.endObjects = new ArrayList<T>();
        }
        this.endObjects.add(o);
    }

    public void addChild(trieNode child) {
        children.add(child);
    }

    public void addChildren(ArrayList<trieNode> children) {
        this.children.addAll(children);
    }

    public trieNode getNextChild() {
        if (this.childPointer < this.children.size()) {
            return this.children.get(this.childPointer++);
        }
        return null;
    }

    public void resetChildPtr() {
        this.childPointer = 0;
    }

    public trieNode getChild(int i) {
        if (i < this.children.size()) {
            return this.children.get(i);
        }
        return null;
    }
}
