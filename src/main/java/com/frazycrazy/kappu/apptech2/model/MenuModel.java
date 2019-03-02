package com.frazycrazy.kappu.apptech2.model;

import java.util.ArrayList;
import java.util.List;

public class MenuModel {

    private List<String> menus = new ArrayList<>();

    public List<String> findAll(){
        return  menus;
    }

    public void add(String item){
        menus.add(item);
    }
}
