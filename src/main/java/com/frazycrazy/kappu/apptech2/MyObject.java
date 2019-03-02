package com.frazycrazy.kappu.apptech2;

class MyObject {

    private String imgUrl;
    private String imgTitle;
    private String imgDest;
    private String mainUrl;
    private String data_Type;

    MyObject(String imgUrl, String imgTitle, String imgDest, String mainUrl, String data_Type) {
        this.imgUrl = imgUrl;
        this.imgTitle = imgTitle;
        this.imgDest = imgDest;
        this.mainUrl = mainUrl;
        this.data_Type = data_Type;
    }

    String getImgUrl() {
        return imgUrl;
    }

    String getImgTitle() {
        return imgTitle;
    }

    String getImgDest() {
        return imgDest;
    }


    String getMainUrl() {
        return mainUrl;
    }


    String getData_Type() {
        return data_Type;
    }

}
