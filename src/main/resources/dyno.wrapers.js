function _dynoRead(name, actual, lineNo) {
    // Send info here

    return actual;
}

function _dynoWrite(varName, newValue, lineNo) {
    // Send info here
    if (Object.prototype.toString.call(newValue) === '[Object object]'
        || Object.prototype.toString.call(newValue) === '[Object function]') {
        // The new value is a 'complex' object, make a note of it
     
        // TODO: Add more complex types to above 'if' statement
    }

    return newValue;
}

function _dynoWriteReturnValue(varName, returnValue, lineNo) {
    // Send info here
    
    return returnValue;
}

function _dynoReadProp(propAsString, lineNo) {
    // Send info here

    return propAsString;
}

function _dynoFunc(functionName, actualFunction, lineNo) {
    // Send info here

    return actualFunction;
}

