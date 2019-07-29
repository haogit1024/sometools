package com.czh.util.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "xml")
public class WePayRequestDto {
    private String service;
    private String version;
    private String charset;
    private String sign_type;
    private String mch_id;
    private String is_raw;
    private String out_trade_no;
    private String device_info;
    private String body;
    private String sub_openid;
    private String sub_appid;
    private String attach;
    private Integer total_fee;
    private String mch_create_ip;
    private String notify_url;
    private String callback_url;
    private String time_start;
    private String good_tag;
    private String nonce_str;

    public WePayRequestDto(){}

    public WePayRequestDto(String service, String version, String charset, String sign_type, String mch_id
            , String is_raw, String out_trade_no, String device_info, String body, String sub_openid, String sub_appid
            , String attach, Integer total_fee, String mch_create_ip, String notify_url, String callback_url
            , String time_start, String good_tag, String nonce_str) {
        this.service = service;
        this.version = version;
        this.charset = charset;
        this.sign_type = sign_type;
        this.mch_id = mch_id;
        this.is_raw = is_raw;
        this.out_trade_no = out_trade_no;
        this.device_info = device_info;
        this.body = body;
        this.sub_openid = sub_openid;
        this.sub_appid = sub_appid;
        this.attach = attach;
        this.total_fee = total_fee;
        this.mch_create_ip = mch_create_ip;
        this.notify_url = notify_url;
        this.callback_url = callback_url;
        this.time_start = time_start;
        this.good_tag = good_tag;
        this.nonce_str = nonce_str;
    }
}
