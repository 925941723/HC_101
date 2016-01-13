package cn.huicheng.hc_101.util;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

/**
 * Created by Administrator on 2015/12/15.
 */
public class UpdataInfoParser {
    /*
* 用pull解析器解析服务器返回的xml文件 (xml封装了版本号)
*/
    public static UpdataInfo getUpdataInfo(InputStream is) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "utf-8");//设置解析的数据源
        int type = parser.getEventType();
        UpdataInfo info = new UpdataInfo();//实体
        while(type != XmlPullParser.END_DOCUMENT ){
            switch (type) {
                case XmlPullParser.START_TAG:
                    if("version".equals(parser.getName())){
                        info.setVersion(parser.nextText());	//获取版本号
                    }else if ("apk_url".equals(parser.getName())){
                        info.setApk_url(parser.nextText());	//获取要升级的APK文件
                    }else if ("xml_url".equals(parser.getName())){
                        info.setXml_url(parser.nextText());	//获取要升级的xml地址
                    }else if ("tip".equals(parser.getName())){
                        info.setTip(parser.nextText());	//获取提示信息
                    }
                    break;
            }
            type = parser.next();
        }
        return info;
    }
}
