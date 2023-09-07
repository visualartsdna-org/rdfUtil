package tsh

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.markdownj.MarkdownProcessor;

// https://github.com/myabc/markdownj
class MarkdownTest {

	@Test
	void test() {
		def base = "C:/temp/git/cwvaContent/ttl/topics"
		
		
        MarkdownProcessor markup = new MarkdownProcessor()
        def s = markup.markdown(new File("$base/dingus.txt").text)
		println s
	}

}
