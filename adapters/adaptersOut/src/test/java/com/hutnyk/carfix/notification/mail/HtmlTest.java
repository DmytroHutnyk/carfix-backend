package com.hutnyk.carfix.notification.mail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class HtmlTest {

    @Test
    public void test_escape_replaces_the_five_html_special_characters() {
        //when
        String result = Html.escape("<b>Tom & \"Jerry\" 'x'</b>");
        //then
        assertThat(result).isEqualTo("&lt;b&gt;Tom &amp; &quot;Jerry&quot; &#39;x&#39;&lt;/b&gt;");
    }

    @Test
    public void test_escape_turns_null_into_an_empty_string() {
        //when + then
        assertThat(Html.escape(null)).isEmpty();
    }
}
