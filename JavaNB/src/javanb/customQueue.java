/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 *
 * @author srivid
 */
public class customQueue {

    private class node {
        public Object data;
        public node link;
        
        public node(Object item) {
            this.data = item;
            this.link = null;
        }
    };
    
    private node head;
    private node end;
    
    public customQueue() {
        head = null;
    }
    
    public void enqueue(Object item) {
        node temp = new node(item);
        if(head == null) {
            head = end = temp;
        } else {
            end.link = temp;
            end = end.link;
        }
    }
    
    public Object dequeue() throws Exception {
        if(head == null) {
            throw new Exception("queue is empty");
        } else {
            Object temp = head.data;
            if(end == head) {
                end = end.link;
            }
            head = head.link;
            return temp;
        }
    }
    
    public boolean isEmpty() {
        return head == null;
    }
        
}
