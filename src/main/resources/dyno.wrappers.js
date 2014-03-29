var counter = 0;

function _dynoRead(varName, value, lineNo, id) {
window.console.log("_dynoRead", varName, Object.prototype.toString.call(value), lineNo);
    // Send info here
    send(JSON.stringify({
             messageType: "VARIABLE_READ",
             lineNo: lineNo,
             value: Object.prototype.toString.call(value),
             variable: varName,
             order: counter++,
             globalID: id
    }));
    
    return value;
}

function _dynoReadAsArg(varName, value, argNumber, lineNo, id) {
window.console.log("_dynoReadAsArg", varName, value, argNumber, lineNo);
    send(JSON.stringify({
             messageType: "READ_AS_ARGUMENT",
             lineNo: lineNo,
             value: Object.prototype.toString.call(value),
             variable: varName,
             argumentNumber: argNumber,
             order: counter++,
             globalID: id
    }));
    return value;
}

function _dynoWrite(varName, newValue, lineNo, id) {
window.console.log("_dynoWrite", varName, newValue, lineNo);
    // Send info here
    send(JSON.stringify({
             messageType: "VARIABLE_WRITE",
             lineNo: lineNo,
             value: Object.prototype.toString.call(newValue),
             variable: varName,
             order: counter++,
             globalID: id
    }));


  /*  if (Object.prototype.toString.call(newValue) === '[Object object]'
        || Object.prototype.toString.call(newValue) === '[Object function]') {
        // The new value is a 'complex' object, make a note of it
     
        // TODO: Add more complex types to above 'if' statement
    }
*/
    return newValue;
}

function _dynoWriteReturnValue(varName, returnValue, lineNo, id) {
window.console.log("_dynoWriteReturnValue", varName, returnValue, lineNo);
    // Send info here
    send(JSON.stringify({
             messageType: "WRITE_RETURN_VALUE",
             lineNo: lineNo,
             value: Object.prototype.toString.call(returnValue),
             variable: varName,
             order: counter++,
             globalID: id
    }));
    
    return returnValue;
}

function _dynoReadProp(baseObject, propAsString, lineNo, id) {
window.console.log("_dynoReadProp", baseObject, propAsString, lineNo);
    // Send info here
    send(JSON.stringify({
             messageType: "PROPERTY_READ",
             lineNo: lineNo,
             variable: baseObject,
             property: propAsString,
             order: counter++,
             globalID: id
    }));

    return propAsString;
}

function _dynoFunc(functionName, actualFunction, lineNo) {
    // Send info here

    return actualFunction;
}

