module com.hulles.alixia.nodeserver {
    exports com.hulles.alixia.nodeserver.pages;
    exports com.hulles.alixia.nodeserver;
    exports com.hulles.alixia.nodeserver.v8;
    
    requires com.google.common;
    requires transitive com.hulles.alixia;
    requires transitive com.hulles.alixia.api;
    requires com.hulles.alixia.media;
    requires transitive java.json;
    requires org.slf4j;
    requires transitive j2v8.linux.x86.x64;
}