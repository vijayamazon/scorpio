package com.onlymaker.scorpio.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class SkuStatsTest {

    @Test
    public void skuStats() throws IOException {
        String sku = "xy624";
        String url = "https://asin.onlymaker.com/Report";
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("sku", sku));
        post.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);
        ObjectMapper mapper = new ObjectMapper();
        Object object = mapper.readValue(content, Object.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object));
    }
}
