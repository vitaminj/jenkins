/*
 * The MIT License
 *
 * Copyright 2012 Jesse Glick.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.model;

import hudson.XmlFile;
import java.io.File;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;

public class CauseTest {

    @Rule public JenkinsRule j = new JenkinsRule();

    @Bug(14814)
    @Test public void deeplyNestedCauses() throws Exception {
        FreeStyleProject a = j.createFreeStyleProject("a");
        FreeStyleProject b = j.createFreeStyleProject("b");
        Run<?,?> early = null;
        Run<?,?> last = null;
        for (int i = 1; i <= 15; i++) {
            last = b.scheduleBuild2(0, new Cause.UpstreamCause((Run<?,?>) a.scheduleBuild2(0, last == null ? null : new Cause.UpstreamCause(last)).get())).get();
            if (i == 5) {
                early = last;
            }
        }
        String buildXml = new XmlFile(Run.XSTREAM, new File(early.getRootDir(), "build.xml")).asString();
        assertTrue("keeps full history:\n" + buildXml, buildXml.contains("<upstreamBuild>1</upstreamBuild>"));
        buildXml = new XmlFile(Run.XSTREAM, new File(last.getRootDir(), "build.xml")).asString();
        assertFalse("too big:\n" + buildXml, buildXml.contains("<upstreamBuild>1</upstreamBuild>"));
    }

}
