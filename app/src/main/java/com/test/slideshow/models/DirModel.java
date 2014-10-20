package com.test.slideshow.models;

/**
 * Created by Nikita on 19.10.2014.
 */
final public class DirModel {
    private String mName;

    public DirModel(String name){
        mName = name;
    }

    public String getName() { return mName; }

    public boolean isEmpty() { return  mName.equals(""); }

    @Override
    public String toString(){
        return mName;
    }
}
