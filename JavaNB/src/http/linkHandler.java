/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javanb.customQueue;
import javanb.userpackage.userException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author srivid
 */
public class linkHandler {

    private URL url;
    private String title;
    private String description;
    private String keywords;
    private LinkedList<URL> images;
    private htmlTreeNode root;
    private String tags;
    private HashSet<URL> anchors;
    private boolean fetchAnchors = false;

    public HashSet<URL> getAnchors() {
        return anchors;
    }

    public String getDescription() {
        return description;
    }

    public LinkedList<URL> getImages() {
        return images;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getTags() {
        return tags;
    }

    public String getTitle() {
        return title;
    }

    public htmlTreeNode getRoot() {
        return root;
    }

    private enum tagType {

        root,
        open,
        close,
        openClose,
        content
    };

    public class htmlTreeNode {

        private String tag;
        private String data;
        private tagType type;
        private ArrayList<htmlTreeNode> children;
        private htmlTreeNode parent;

        public htmlTreeNode(String tag, String data, tagType type, htmlTreeNode parent) {
            this.tag = tag;
            this.data = data;
            this.type = type;
            this.parent = parent;
        }

        public htmlTreeNode(String data, tagType type, htmlTreeNode parent) {
            this.data = data;
            this.type = type;
            this.parent = parent;
        }

        public void addChild(htmlTreeNode node) {
            if (this.children == null) {
                this.children = new ArrayList<htmlTreeNode>();
            }
            this.children.add(node);
        }

        public void addChild(ArrayList<htmlTreeNode> nodes) {
            if (this.children == null) {
                this.children = new ArrayList<htmlTreeNode>();
            }
            this.children.addAll(nodes);
        }

        public String getData() {
            return this.data;
        }

        public tagType getType() {
            return this.type;
        }

        public String getTag() {
            return this.tag;
        }

        public htmlTreeNode getParent() {
            return this.parent;
        }

        public void setParent(htmlTreeNode node) {
            this.parent = node;
        }

        public ArrayList<htmlTreeNode> getAllChildren() {
            return this.children;
        }

        @Override
        public String toString() {
            return "tag:" + this.tag + ",type:" + this.type + ",data:" + this.data;
        }
    }

    public linkHandler(URL url) throws userException {
        try {
            this.url = url;
            this.title = "";
            this.description = "";
            this.images = new LinkedList<URL>();
            this.keywords = "";
            this.tags = "";
            this.anchors = new HashSet<URL>();
            this.extractLink();
        } catch (Exception ex) {
            throw new userException("error occured while extracting link:" + ex.getMessage());
        }
    }

    public linkHandler(URL url, boolean fetchAnchors) throws userException {
        try {
            this.url = url;
            this.title = "";
            this.description = "";
            this.images = new LinkedList<URL>();
            this.keywords = "";
            this.tags = "";
            this.anchors = new HashSet<URL>();
            this.fetchAnchors = fetchAnchors;
            this.extractLink();
        } catch (Exception ex) {
            Logger.getLogger(linkHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getContent() throws userException {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() >= 400) {
                return null;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine, res = "";
            while ((inputLine = in.readLine()) != null) {
                res += (inputLine);
            }
            return res;
        } catch (IOException ex) {
            throw new userException("error occured while connecting to url:" + ex.getMessage());
        }
    }

    private Hashtable getType(String str, int index, int len) {
        if (index < len) {
            Hashtable hash = new Hashtable();
            char c = str.charAt(index);
            String tag = "", data;
            tagType type;
            htmlTreeNode node;
            if (c == '<') {
                data = "";
                if (index + 1 < len && str.charAt(index + 1) == '/') {
                    type = tagType.close;
                    index += 2;
                    while (index < len) {
                        char cAtIndex = str.charAt(index);
                        index++;
                        if (cAtIndex == '>') {
                            break;
                        }
                        data += cAtIndex;
                    }
                } else {
                    type = tagType.open;
                    index++;
                    while (index < len) {
                        char cAtIndex = str.charAt(index);
                        index++;
                        if (cAtIndex == '/' && index + 1 < len && str.charAt(index + 1) == '>') {
                            type = tagType.openClose;
                            break;
                        }
                        if (cAtIndex == '>') {
                            type = tagType.open;
                            break;
                        }
                        data += cAtIndex;
                    }
                }
                String[] dataArr = data.split(" ");
                if (dataArr.length > 0) {
                    tag = dataArr[0].trim().toLowerCase();
                }
            } else {
                type = tagType.content;
                data = "";
                while (index < len) {
                    char cAtIndex = str.charAt(index);
                    if (cAtIndex == '<') {
                        break;
                    }
                    data += cAtIndex;
                    index++;
                }
            }
            hash.put("tag", tag);
            hash.put("data", data);
            hash.put("type", type);
            hash.put("nextIndex", index);
            return hash;
        }
        return null;
    }

    public int ignoreDocType(String str, int len) {
        int index = str.indexOf("<html");
        if (index == -1) {
            index = str.indexOf("<HTML");
        }
        return index;
    }

    public void parse(String str) throws userException {
        int len = str.length();
        int initPoint = this.ignoreDocType(str, len);
        int iter = initPoint;
        if (initPoint == -1) {
            throw new userException("invalid HTML Doc. Check the syntax");
        }
        root = new htmlTreeNode("#", tagType.root, null);
        htmlTreeNode temp = root;
        while (true) {
            if (iter >= len) {
                break;
            }
            if (temp == null) {
                temp = root;
            }
            Hashtable hash = this.getType(str, iter, len);
            if (hash != null) {
                tagType type = (tagType) hash.get("type");
                htmlTreeNode node;
                switch (type) {
                    case open:
                        node = new htmlTreeNode((String) hash.get("tag"), (String) hash.get("data"), tagType.open, temp);
                        temp.addChild(node);
                        temp = node;
                        break;
                    case openClose:
                        node = new htmlTreeNode((String) hash.get("tag"), (String) hash.get("data"), tagType.open, temp);
                        temp.addChild(node);
                        break;
                    case close:
                        while (temp != null && !hash.get("tag").equals(temp.getTag())) {
                            temp = temp.getParent();
                        }
                        if (temp != null) {
                            temp = temp.getParent();
                        }
                        break;
                    case content:
                        node = new htmlTreeNode((String) hash.get("data"), tagType.content, temp);
                        temp.addChild(node);
                        break;
                    default:
                        break;
                }
            } else {
                throw new userException("invalid HTML Doc. Check the syntax");
            }
            iter = ((Integer) hash.get("nextIndex")).intValue();
        }
    }

    public void printHtmlTree() throws Exception {
        customQueue q = new customQueue();
        q.enqueue(this.root);
        while (true) {
            if (q.isEmpty()) {
                break;
            }
            htmlTreeNode node = (htmlTreeNode) q.dequeue();
            System.out.println(node.toString());
            ArrayList<htmlTreeNode> children = node.getAllChildren();
            if (children != null) {
                for (htmlTreeNode child : children) {
                    q.enqueue(child);
                }
            }

        }
    }

    public void extractLink() throws userException, Exception {
        String result = this.getContent();
        if (result != null) {
            this.parse(result);
            customQueue q = new customQueue();
            q.enqueue(this.root);
            while (true) {
                if (q.isEmpty()) {
                    break;
                }
                htmlTreeNode node = (htmlTreeNode) q.dequeue();
                if (node != null) {
                    if (node.getTag() != null && node.getTag().equals("title")) {
                        ArrayList<htmlTreeNode> nodeChildren = node.getAllChildren();
                        if (nodeChildren != null) {
                            for (htmlTreeNode child : nodeChildren) {
                                if (child.getType() == tagType.content) {
                                    this.title += child.getData();
                                }
                            }
                        }
                    } else if ( node.getTag() != null && node.getTag().equals("meta")) {
                        String data = node.getData();
                        String descReg1 = "meta\\s+(name=\\s*[\"]?[d|D]escription[\"]?)\\s+content=[\"]?(.*)[\"]?";
                        String descReg2 = "meta\\s+content=\\s*[\"]?(.*)[\"]?\\s+(name=[\"]?[d|D]escription[\"]?)";
                        String keyReg1 = "meta\\s+(name=\\s*[\"]?[k|K]eywords[\"]?)\\s+content=[\"]?(.*)[\"]?";
                        String keyReg2 = "meta\\s+content=\\s*[\"]?(.*)[\"]?\\s+(name=[\"]?[k|K]eywords[\"]?)";
                        if (data.matches(descReg1)) {
                            Pattern pattern = Pattern.compile(descReg1);
                            Matcher matcher = pattern.matcher(data);
                            while (matcher.find()) {
                                this.description += matcher.group(2);
                            }
                        } else if (data.matches(descReg2)) {
                            Pattern pattern = Pattern.compile(descReg2);
                            Matcher matcher = pattern.matcher(data);
                            while (matcher.find()) {
                                this.description += matcher.group(1);
                            }
                        } else if (data.matches(keyReg1)) {
                            Pattern pattern = Pattern.compile(keyReg1);
                            Matcher matcher = pattern.matcher(data);
                            while (matcher.find()) {
                                this.keywords += matcher.group(2);
                            }
                        } else if (data.matches(keyReg2)) {
                            Pattern pattern = Pattern.compile(keyReg2);
                            Matcher matcher = pattern.matcher(data);
                            while (matcher.find()) {
                                this.keywords += matcher.group(1);
                            }
                        }
                    } else if (node.getTag() != null && node.getTag().equals("img")) {
                        String data = node.getData();
                        String regex = "src=[\"]?([^\\s]*)(.*)";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(data);
                        while (matcher.find()) {
                            String imageUrl = matcher.group(1);
                            imageUrl = StringUtils.strip(imageUrl, "\"");
                            URL image = new URL(this.url, imageUrl);
                            images.add(image);
                        }
                    } else if (fetchAnchors && node.getTag() != null && node.getTag().equals("a")) {
                        String data = node.getData();
                        String regex = "href=\"([^\"#]+)\"";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(data);
                        while (matcher.find()) {
                            URL href = new URL(this.url, matcher.group(1));
                            this.anchors.add(href);
                        }
                    }
                }
                ArrayList<htmlTreeNode> children = node.getAllChildren();
                if (children != null) {
                    for (htmlTreeNode child : children) {
                        q.enqueue(child);
                    }
                }
            }
            this.populateTags();
        } else {
            throw new userException("error fetching the url");
        }
    }

    public void populateTags() {
        String[] titleArr = this.title.split(" ");
        String[] descArr = this.description.split(" ");
        this.tags = this.keywords;
        for (String titleString : titleArr) {
            try {
                if (!miscellaneous.misc.isStopWord(titleString)) {
                    this.tags += "," + titleString;
                }
            } catch (userException ex) {
                Logger.getLogger(linkHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (String descString : descArr) {
            try {
                if (!miscellaneous.misc.isStopWord(descString)) {
                    this.tags += "," + descString;
                }
            } catch (userException ex) {
                Logger.getLogger(linkHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String toString() {
        String str = "Title=" + this.title + ", description=" + this.description + ", keywords=" + this.keywords + "\nimages=\n";
        while (!this.images.isEmpty()) {
            str += this.images.pop() + "\n";
        }
        return str + ",anchors:" + this.anchors.toString();
    }
}
