exclude :test_check_inheritable, "TypeError expected but nothing was raised."
exclude :test_clone_when_method_exists_on_singleton_class_of_singleton_class, "NoMethodError: undefined method `s2_method' for #<Class:#<#<Class:0x5a8>:0x5c8>>"
exclude :test_constant_access_from_method_in_cloned_class, "NameError: uninitialized constant TestClass::CloneTest::TEST"
exclude :test_invalid_superclass, "Expected Exception(TypeError) was raised, but the message doesn't match. Expected /M🗿/ to match \"superclass must be a Class\"."
exclude :test_method_redefinition, "Expected /:190: warning: method redefined; discarding old foo/ to match \"\"."
exclude :test_redefine_private_class, "NameError expected but nothing was raised."
exclude :test_redefinition_mismatch, "Expected \"1:Integer is not a class\" to include \"/b/b/e/main/test/mri/tests/ruby/test_class.rb:709: previous definition\"."
exclude :test_uninitialized, "Exception(TypeError) with message matches to /prohibited/."
exclude :test_visibility_inside_method, "expected: /calling private without arguments inside a method may not have the intended effect/"
