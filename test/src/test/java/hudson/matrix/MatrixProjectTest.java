package hudson.matrix;

import hudson.model.Result;
import hudson.tasks.Ant;
import hudson.tasks.Maven;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SingleFileSCM;

import java.util.List;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class MatrixProjectTest extends HudsonTestCase {
    /**
     * Tests that axes are available as build variables in the Ant builds.
     */
    public void testBuildAxisInAnt() throws Exception {
        MatrixProject p = createMatrixProject();
        p.getBuildersList().add(new Ant("-Dprop=${db} test",null,null,null,null));

        // we need a dummy build script that echos back our property
        p.setScm(new SingleFileSCM("build.xml","<project><target name='test'><echo>assertion ${prop}=${db}</echo></target></project>"));

        MatrixBuild build = p.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(4,runs.size());
        for (MatrixRun run : runs) {
            assertBuildStatus(Result.SUCCESS, run);
            String expectedDb = run.getParent().getCombination().get("db");
            assertLogContains("assertion "+expectedDb+"="+expectedDb, run);
        }
    }

    /**
     * Tests that axes are available as build variables in the Maven builds.
     */
    public void testBuildAxisInMaven() throws Exception {
        MatrixProject p = createMatrixProject();
        p.getBuildersList().add(new Maven("-Dprop=${db} validate",null));

        // we need a dummy build script that echos back our property
        p.setScm(new SingleFileSCM("pom.xml",getClass().getResource("echo-property.pom")));

        MatrixBuild build = p.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(4,runs.size());
        for (MatrixRun run : runs) {
            assertBuildStatus(Result.SUCCESS, run);
            String expectedDb = run.getParent().getCombination().get("db");
            assertLogContains("assertion "+expectedDb+"="+expectedDb, run);
        }
    }

    @Override
    protected MatrixProject createMatrixProject() throws IOException {
        MatrixProject p = super.createMatrixProject();

        // set up 2x2 matrix
        AxisList axes = new AxisList();
        axes.add(new Axis("db","mysql","oracle"));
        axes.add(new Axis("direction","north","south"));
        p.setAxes(axes);

        return p;
    }
}