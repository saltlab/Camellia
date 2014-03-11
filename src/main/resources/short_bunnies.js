var tt = "hello";

tt = {
    prop: "world!"
}; 

tt = "goodbye";

tt.hello.goodbye;

var dd = tt;

dd = tt;

callMe(otherCall(), tt.prop1.prop2());

dd = 0;
