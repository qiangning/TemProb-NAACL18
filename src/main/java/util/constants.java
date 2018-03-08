package util;

import java.util.HashSet;
import java.util.Set;

public class constants {
    public static  HashSet<String> connectivesSet = new HashSet<String>(){{
        add("before");add("after");add("since");add("hence");add("thus");
        add("because");add("so");add("when");add("while");add("previously");add("eventually");
        add("initially");add("recently");add("meanwhile");add("lately");add("afterwards");
        add("if");add("although");add("however");add("nevertheless");add("otherwise");
        add("therefore");add("even if");add("even though");
        add("as a result");add("so as to");add("so that");
    }};
    public static  HashSet<String> modalVerbSet = new HashSet<String>(){{
        add("will");add("would");add("can");add("could");add("may");add("might");
    }};
}
