package com.aimedical.modules.ai.api.dto.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class Phase4ServiceMetaCapableTest {

    @Test
    void shouldReturnServiceMetaFromAnonymousImpl() {
        Phase4ServiceMeta expected = new Phase4ServiceMeta("gpt4", 2, 1);
        Phase4ServiceMetaCapable capable = () -> expected;
        assertSame(expected, capable.getServiceMeta());
    }
}
