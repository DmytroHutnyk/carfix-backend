package com.hutnyk.carfix.notification.mail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class EmailLayoutTest {

    @Test
    public void test_wrap_escapes_the_title_keeps_the_body_html_and_renders_the_full_width_table() {
        String result = EmailLayout.wrap("a<b", "<p>x</p>");
        assertThat(result).startsWith("<!doctype html>");
        assertThat(result).contains("<title>a&lt;b</title>");
        assertThat(result).contains("<p>x</p>");
        assertThat(result).contains("width=\"100%\"");
    }
}
