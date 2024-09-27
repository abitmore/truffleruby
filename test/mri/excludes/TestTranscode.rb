exclude :test_encode_xml_multibyte, "Encoding::CompatibilityError: incompatible encoding regexp match (US-ASCII regexp with UTF-16LE string)"
exclude :test_invalid_replace, "Encoding::CompatibilityError: incompatible character encodings: ISO-2022-JP and UTF-8"
exclude :test_newline_options, "<\"A\\n\" + \"B\\n\" + \"C\"> expected but was <\"A\\n\" + \"B\\r\\n\" + \"C\">."
exclude :test_noargument, "Encoding::InvalidByteSequenceError: \"\\x81\" on UTF-8"
exclude :test_pseudo_encoding_inspect, "<\"\\\"\\\\xFE\\\\xFF\\\\x00\\\\x61\\\\x00\\\\x61\\\\x00\\\\x61\\\"\"> expected but was <\"\\\"\\\\uFEFFaaa\\\"\">."
