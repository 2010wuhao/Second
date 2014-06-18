/**
 * =====================================================================
 *
 * @file  MultiTree.java
 * @Module Name   com.joysee.adtv.logic.bean
 * @author songwenxuan
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月20日
 * @brief  This file is the http **** implementation.
 * @This file is responsible by ANDROID TEAM.
 * @Comments: 
 * =====================================================================
 * Revision History:
 *
 *                   Modification  Tracking
 *
 * Author            Date            OS version        Reason 
 * ----------      ------------     -------------     -----------
 * songwenxuan          2014年3月20日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic.bean;

import android.util.Log;

import java.util.ArrayList;

public class MultiTree {

    public static class Node {
        int mType = TreeNodeTypeEmpty; // 节点数据类型
        String mName = "/"; // 节点名字
        Object mData = null; // 节点数据,根据类型强转Integer...
        private ArrayList<Node> mChilds = new ArrayList<Node>(); // 所有子节点

        public void addChild(Node node) {
            if (node == this || node == null) {
                return;
            }

            mChilds.add(node);
        }

        public ArrayList<Node> getChildreds() {
            return mChilds;
        }

        public Object getData() {
            return mData;
        }

        public String getName() {
            return mName;
        }

        public int getType() {
            return mType;
        }

        public void setData(Object data) {
            mData = data;
        }

        public void setName(String name) {
            mName = name;
        }

        public void setType(int type) {
            mType = type;
        }

        public String toString() {
            return "MultiTree$Node[mName= " + mName + ", mType=" + mType + ", mData=" + mData +
                    ", childs=" + mChilds.size() + "]";
        }
    }

    // 需与底层保持一致
    public static final int TreeNodeTypeEmpty = 0; // 没有值
    public static final int TreeNodeTypeU8 = 1; // 1-7数据为Integer
    public static final int TreeNodeTypeS8 = 2;
    public static final int TreeNodeTypeU16 = 3;
    public static final int TreeNodeTypeS16 = 4;
    public static final int TreeNodeTypeU32 = 5;
    public static final int TreeNodeTypeS32 = 6;
    public static final int TreeNodeTypeBool = 7;
    public static final int TreeNodeTypeFloat = 8;
    public static final int TreeNodeTypeDouble = 9;
    public static final int TreeNodeTypeString = 10; // 数据为String

    public static final int TreeNodeTypeBinary = 11;

    private String mTag = "MultiTree";
    private Node mRoot;

    // 如果一个树存在了,必须有一个根节点
    public MultiTree() {
        mRoot = new Node();
    }

    public MultiTree(Node node) {
        mRoot = node;
    }

    // 添加子节点,成功返回子节点,失败返回null
    public Node addChild(Node parent, int type, String name, Object childData) {
        if (parent == null || childData == null) {
            return null;
        }

        Node child = new Node();
        child.mType = type;
        child.mData = childData;
        child.mName = name;
        boolean result = parent.mChilds.add(child);

        if (result == true) {
            return child;
        }

        return null;
    }

    public void addChild(Node parent, Node child) {
        if (parent == child | parent == null || child == null) {
            return;
        }

        parent.mChilds.add(child);
    }

    public Node addChild(Node parent, String name, Object childData) {
        if (parent == null || childData == null) {
            return null;
        }

        Node child = new Node();

        child.mData = childData;
        child.mName = name;

        Class<?> cls = childData.getClass();
        if (cls == Integer.class) {
            child.mType = TreeNodeTypeU32;
        } else if (cls == String.class) {
            child.mType = TreeNodeTypeString;
        } else if (cls == Boolean.class) {
            child.mData = (Boolean) childData ? 1 : 0;
            child.mType = TreeNodeTypeU32;
        } else if (cls == Double.class) {
            child.mType = TreeNodeTypeDouble;
        } else if (cls == Float.class) {
            child.mType = TreeNodeTypeFloat;
        } else if (cls == byte[].class) {
            child.mType = TreeNodeTypeBinary;
        } else {
            Log.d(mTag, "unknow type");
            return null;
        }

        boolean result = parent.mChilds.add(child);

        if (result == true) {
            return child;
        }
        return null;
    }

    // 返回某个节点的子节点个数
    public int childrenNumber(Node node) {
        return node.mChilds.size();
    }

    // 删除子节点
    public boolean eraseChild(Node parent, Node child) {
        if (parent == null || child == null) {
            return false;
        }

        return parent.mChilds.remove(child);
    }

    // 获取某个节点的所有子节点
    public ArrayList<Node> getChildrens(Node node) {
        return node.mChilds;
    }

    public Object getData(Node node) {
        return node.mData;
    }

    // 在当前节点的子节点中名字为name的节点,并返回data值
    // 如果没有找到返回空
    public Object getLeafValueByName(Node node, String name) {
        for (Node n : node.mChilds) {
            if (n.mName.equals(name)) {
                return n.mData;
            }
        }
        return null;
    }

    public String getName(Node node) {
        return node.mName;
    }

    // 在当前节点子节点中查找名字为name的子节点.
    // 如果没有找到返回空
    public Node getNodeByName(Node node, String name) {
        for (Node n : node.mChilds) {
            if (n.mName.equals(name)) {
                return n;
            }
        }
        return null;
    }

    public int getType(Node node) {
        return node.mType;
    }

    // 打印节点及其子节点,,广度
    public void printNode(Node node) {
        ArrayList<Node> list1 = new ArrayList<Node>();
        ArrayList<Node> list2 = new ArrayList<Node>();

        list1.add(node);
        while (!list1.isEmpty()) {
            for (Node n : list1) {
                // 打印当前节点
                Log.d(mTag, n.toString());
                // 收集子节点
                for (Node m : getChildrens(n)) {
                    list2.add(m);
                }
            }

            // 交换列表.
            list1.clear();
            list1.addAll(list2);
            list2.clear();
        }
    }

    // 打印树
    public void printTree() {
        printNode(mRoot);
    }

    // 返回根节点
    public Node root() {
        return mRoot;
    }

    public void setData(Node node, Object data) {
        node.mData = data;
    }

    public void setName(Node node, String name) {
        node.mName = name;
    }

    // 设置node数据
    public void setNodeData(Node node, int type, String name, Object data) {
        node.mData = data;
        node.mType = type;
        node.mName = name;
    }

    // 设置根节点
    // 返回原root节点
    public Node setRoot(Node node) {
        Node ret = mRoot;
        mRoot = node;
        return ret;
    }

    public void setType(Node node, int type) {
        node.mType = type;
    }
}
