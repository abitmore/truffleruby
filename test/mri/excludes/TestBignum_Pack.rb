exclude :test_numbits_2comp, "undefined symbol: rb_absint_numwords"
exclude :test_pack2comp_overflow, "<[-2, \"\\x0F\"]> expected but was <[-1, \"\\xEF\"]>."
exclude :test_pack_argument_check, "ArgumentError expected but nothing was raised."
exclude :test_pack_fixed_buffer, "<[1, \"\\x00\\x01\"]> expected but was <[1, \"\\x01\\x00\"]>."
exclude :test_pack_nail, "<[1, \"\\x01\\x00\\x00\\x00\\x01\\x01\"]> expected but was <[1, \"#\\x00\\x00\\x00\\x00\\x00\"]>."
exclude :test_pack_overflow, "<[-2, \"\\x01\"]> expected but was <[-1, \"\\x11\"]>."
exclude :test_pack_wordsize, "<[1, \"\\x00\\x01\"]> expected but was <[1, \"\\x01\\x00\"]>."
exclude :test_unpack2comp_negative_single_byte, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
exclude :test_unpack2comp_negative_zero, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
exclude :test_unpack2comp_sequence_of_ff, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
exclude :test_unpack2comp_single_byte, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
exclude :test_unpack_argument_check, "[ArgumentError] exception expected, not #<NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby>."
exclude :test_unpack_nail, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
exclude :test_unpack_native_endian, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
exclude :test_unpack_orders, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
exclude :test_unpack_sign, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
exclude :test_unpack_wordorder_and_endian, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
exclude :test_unpack_wordsize, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
exclude :test_unpack_zero, "NotImplementedError: The C API function rb_integer_unpack is not implemented yet on TruffleRuby"
