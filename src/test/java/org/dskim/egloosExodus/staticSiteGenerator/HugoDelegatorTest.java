package org.dskim.egloosExodus.staticSiteGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HugoDelegatorTest {
	@Autowired
	HugoDelegator hugoDelegator;

	@Test
	public void initTest() throws Exception {
		hugoDelegator.init("test", "블로그명");
	}
}
