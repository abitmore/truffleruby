exclude :test_Complex_with_invalid_exception, "ArgumentError expected but nothing was raised."
exclude :test_Complex_without_exception, "RuntimeError: "
exclude :test_add_with_redefining_int_plus, "| <internal:core> core/truffle/ffi/pointer.rb:145:in `check_bounds': Memory access offset=0 size=5 is out of bounds (IndexError)"
exclude :test_canonicalize_internal, "NoMethodError: undefined method `+' for #<#<Class:0x538>:0x558 @real=false>"
exclude :test_coerce2, "<=>."
exclude :test_compsub, "Expected (1+0i) to be an instance of ComplexSub, not Complex."
exclude :test_conv, "Expected (1+0i) (oid=1624) to be the same as (1+0i) (oid=1640)."
exclude :test_div, "Expected (1/1) to be integer?."
exclude :test_expt, "<(-3+4i)> expected but was <(-3.0+4.000000000000002i)>."
exclude :test_fixed_bug, "ArgumentError expected but nothing was raised."
exclude :test_infinite_p, "Expected 1 to be nil."
exclude :test_marshal_compatibility, "Exception raised: <#<NoMethodError: undefined method `<' for nil:NilClass>>"
exclude :test_mul, "<(Infinity+0i)> expected but was <(Infinity+NaN*i)>."
exclude :test_polar, "TypeError: not a real"
exclude :test_rationalize, "NoMethodError: undefined method `abs' for nil:NilClass"
exclude :test_ruby19, "NoMethodError expected but nothing was raised."
exclude :test_sub_with_redefining_int_minus, "| <internal:core> core/truffle/ffi/pointer.rb:145:in `check_bounds': Memory access offset=0 size=42 is out of bounds (IndexError)"
