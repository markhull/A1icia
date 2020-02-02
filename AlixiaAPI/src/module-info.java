/**
 * AlixiaAPI
 * 
 * api
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.api {
    exports com.hulles.alixia.api.jebus;
    exports com.hulles.alixia.api.object;
    exports com.hulles.alixia.api.remote;
    exports com.hulles.alixia.api.dialog;
    exports com.hulles.alixia.api;
    exports com.hulles.alixia.api.tools;
    exports com.hulles.alixia.api.shared;

    requires com.google.common;
    requires transitive com.hulles.alixia.media;
    requires commons.pool2;
    requires java.desktop;
    requires java.json;
    requires jedis;
    requires org.slf4j;
}