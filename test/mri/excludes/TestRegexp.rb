exclude :test_backref_overrun, "Expected Exception(SyntaxError) was raised, but the message doesn't match. Expected /invalid backref number/ to match \"(eval):1: Index 90000 out of bounds for length 5: /(())(?<X>)((?(90000)))/\"."
exclude :test_bug_19467, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_bug_20453, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_cache_index_initialize, "is too slow so leads to timeouts on CI"
exclude :test_cache_opcodes_initialize, "is too slow so leads to timeouts on CI"
exclude :test_dup, "[TypeError] exception expected, not #<FrozenError: can't modify frozen Regexp: //>."
exclude :test_dup_warn, "expected: /duplicated/"
exclude :test_extended_comment_invalid_escape_bug_18294, "assert_separately failed with error message"
exclude :test_ignorecase, "expected: /variable \\$= is no longer effective; ignored/"
exclude :test_initialize, "expected: /ignored/"
exclude :test_inspect, "<\"/\\\\x00/i\"> expected but was <\"/\\u0000/i\">."
exclude :test_invalid_free_at_parse_depth_limit_over, "NameError: uninitialized constant Bug"
exclude :test_linear_time_p, "Expected Regexp.linear_time?(/(?=(a))/) to return false."
exclude :test_match_cache_atomic, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_match_cache_atomic_complex, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_match_cache_exponential, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_match_cache_negative_look_ahead, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_match_cache_negative_look_behind, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_match_cache_positive_look_ahead, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_match_cache_positive_look_ahead_complex, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_match_cache_positive_look_behind, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_match_cache_square, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_match_cache_with_peek_optimization, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_match_data_named_captures, "ArgumentError: wrong number of arguments (given 1, expected 0)"
exclude :test_match_no_match_no_matchdata, "NoMethodError: undefined method `count_objects' for ObjectSpace:Module"
exclude :test_match_without_regexp, "Encoding::CompatibilityError: incompatible encoding regexp match (Shift_JIS regexp with UTF-8 string)"
exclude :test_matchdata, "<42> expected but was <nil>."
exclude :test_named_capture, "<\"#<MatchData \\\"&amp; y\\\" foo:\\\"amp\\\" foo:\\\"y\\\">\"> expected but was <\"#<MatchData \\\"&amp; y\\\" foo:\\\"amp\\\" 2:\\\"y\\\">\">."
exclude :test_named_capture_nonascii, "IndexError expected but nothing was raised."
exclude :test_once_multithread, "transient(fails on CI): <[/1/, /1/]> expected but was <[/1/]>."
exclude :test_parse, "Polyglot::ForeignException: invalid group reference 80"
exclude :test_parse_kg, "Polyglot::ForeignException: undefined name <-1> reference"
exclude :test_posix_bracket, "expected: /duplicated range/"
exclude :test_regsub, "RuntimeError expected but nothing was raised."
exclude :test_s_timeout, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_s_timeout_corner_cases, "NoMethodError: private method `timeout' called for Regexp:Class"
exclude :test_timeout_corner_cases, "NoMethodError: private method `timeout' called for //:Regexp"
exclude :test_timeout_longer_than_global, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_timeout_nil, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_timeout_shorter_than_global, "NoMethodError: undefined method `timeout=' for Regexp:Class"
exclude :test_to_s, "Encoding::CompatibilityError: incompatible character encodings: UTF-16BE and UTF-8"
exclude :test_to_s_under_gc_compact_stress, "Encoding::CompatibilityError: incompatible character encodings: UTF-16BE and UTF-8"
exclude :test_unescape, "[ArgumentError] exception expected, not #<RegexpError: invalid Unicode range: /\\u{ ffffff }/>."
exclude :test_unicode_age, "RegexpError: invalid character property name <ag>: /\\A\\p{age=6.0}+\\z/"
exclude :test_unicode_age_14_0, "RegexpError: invalid character property name <age>: /\\A\\p{age=14.0}+\\z/"
exclude :test_unicode_age_15_0, "RegexpError: invalid character property name <age>: /\\A\\p{age=15.0}+\\z/"
exclude :test_uninitialized, "TypeError: allocator undefined for Regexp"
exclude :test_union, "<\"(?-mix:\\\\/)|\"> expected but was <\"(?-mix:/)|\">."
exclude :test_union2, "</(?-mix:\\u3042)|(?-mix:\\u3042)/> expected but was </(?-mix:あ)|(?-mix:あ)/>."
