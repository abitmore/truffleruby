exclude :test_block_parameter_should_not_create_objects, "NoMethodError: undefined method `count_objects' for ObjectSpace:Module"
exclude :test_callinfo_unreachable_path, "NameError: uninitialized constant RubyVM"
exclude :test_clear_unreachable_keyword_args, "NameError: uninitialized constant RubyVM"
exclude :test_eqq, "assert_separately failed with error message"
exclude :test_fixnum_gt, "assert_separately failed with error message"
exclude :test_fixnum_lt, "very slow: 600.25s on truffleruby 24.2.0-dev-b555f590, like ruby 3.2.4, GraalVM CE JVM [x86_64-linux] with AMD Ryzen 7 3700X 8-Core Processor (16 vCPUs)"
exclude :test_fixnum_minus, "very slow: 600.20s on truffleruby 24.2.0-dev-b555f590, like ruby 3.2.4, GraalVM CE JVM [x86_64-linux] with AMD Ryzen 7 3700X 8-Core Processor (16 vCPUs)"
exclude :test_fixnum_plus, "assert_separately failed with error message"
exclude :test_hash_aref_with, "assert_separately failed with error message"
exclude :test_objtostring, "assert_separately failed with error message"
exclude :test_opt_case_dispatch, "NameError: uninitialized constant TestRubyOptimization::RubyVM"
exclude :test_optimized_rescue, "| truffleruby: No such file or directory --  (LoadError)"
exclude :test_peephole_dstr, "NameError: uninitialized constant TestRubyOptimization::RubyVM"
exclude :test_peephole_optimization_without_trace, "NameError: uninitialized constant RubyVM"
exclude :test_peephole_string_literal_range, "NameError: uninitialized constant TestRubyOptimization::RubyVM"
exclude :test_string_freeze, "Expected \"foo\" to be nil."
exclude :test_string_freeze_block, "<true> expected but was <\"block\">."
exclude :test_string_freeze_saves_memory, "IO buffer NOT resized prematurely because will likely be reused."
exclude :test_string_ltlt, "assert_separately failed with error message"
exclude :test_string_plus, "NoMethodError: undefined method `unpack1' for nil:NilClass"
exclude :test_string_uminus, "Expected \"foo\" to be nil."
exclude :test_tailcall, "NameError: uninitialized constant TestRubyOptimization::RubyVM"
exclude :test_tailcall_condition_block, "NameError: uninitialized constant TestRubyOptimization::RubyVM"
exclude :test_tailcall_inhibited_by_block, "NameError: uninitialized constant TestRubyOptimization::RubyVM"
exclude :test_tailcall_inhibited_by_rescue, "NameError: uninitialized constant TestRubyOptimization::RubyVM"
exclude :test_tailcall_not_to_grow_stack, "NameError: uninitialized constant TestRubyOptimization::RubyVM"
exclude :test_tailcall_symbol_block_arg, "NameError: uninitialized constant TestRubyOptimization::RubyVM"
exclude :test_tailcall_with_block, "NameError: uninitialized constant TestRubyOptimization::RubyVM"
exclude :test_trace_optimized_methods, "ArgumentError: unknown event: c_call"
