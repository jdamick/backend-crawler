#!/usr/bin/env groovy
// Generates server-side metadata for Gradle auto-installation
@GrabResolver(name="m.g.o-public",root='http://maven.glassfish.org/content/group/public/')
@Grab(group="org.jvnet.hudson",module="htmlunit",version="2.2-hudson-9")
import com.gargoylesoftware.htmlunit.html.*;

@Grab(group="org.kohsuke.stapler",module="json-lib",version="2.1",classifier="jdk15")
import net.sf.json.*
import com.gargoylesoftware.htmlunit.WebClient

def wc = new WebClient()
HtmlPage p = wc.getPage("http://repo.gradle.org/gradle/distributions/");

def json = [];

p.selectNodes("//div[@class='local-repos-list']//a[@href]").reverse().collect { HtmlAnchor e ->
    def url = e.getHrefAttribute()
    println url
    def m = (url =~ /gradle-(.*)-bin.zip$/)
    if (m) {
        json << ["id":m[0][1], "name": "Gradle ${m[0][1]}".toString(), "url":url];
    }
}

JSONObject envelope = JSONObject.fromObject([list:json]);
println envelope.toString(2)

key = "hudson.plugins.gradle.GradleInstaller";
File d = new File("target")
d.mkdirs()
new File(d,"${key}.json").write("downloadService.post('${key}',${envelope.toString(2)})");
