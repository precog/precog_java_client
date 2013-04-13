package com.precog.client.rest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static java.net.URLEncoder.encode;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;


/**
 * Class to handle the rest actions
 *
 * @author Tom Switzer <switzer@precog.com>
 */
class Response {
//    private final int statusCode;
//    private final String status;
//    private final String content;
//
//    Response(int statusCode, String status, String content) {
//        this.statusCode = statusCode;
//        this.status = status;
//        this.content = content;
//    }
//
//    public String expect(int codes...) {
//        boolean valid = false;
//        for (code : codes) {
//            if (code == statusCode) {
//                valid = true;
//            }
//        }
//
//        if (!valid) {
//            throw new IOException();
//            //        "Unexpected response from server: " + conn.getResponseCode() + ": " + conn.getResponseMessage() +
//            //                " ; service url " + serviceURL +
//            //                " ; " + (request.getBody().length() > 0 ? "record body '" + request.getBody() + "'" : " no body"));
//        } else {
//          return content;
//        }
//    }
}
