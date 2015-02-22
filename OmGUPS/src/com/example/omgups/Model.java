package com.example.omgups;

public class Model {

    private String name;
    private String id;
    private boolean selected;
    private boolean marked;

    public Model(String name, String id) {
        this.name = name;
        this.id = id;
        selected = false;
        marked = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

}
