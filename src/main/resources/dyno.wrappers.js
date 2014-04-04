var counter = 0;

function _dynoRead(varName, value, lineNo, id) {
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

function _dynoReadAsArg(varName, value, functionName, argNumber, lineNo, id) {
    send(JSON.stringify({
             messageType: "READ_AS_ARGUMENT",
             lineNo: lineNo,
             value: Object.prototype.toString.call(value),
             variable: varName,
             argumentNumber: argNumber,
             functionName: functionName,
             order: counter++,
             globalID: id
    }));
    return value;
}

function _dynoWrite(varName, newValue, readFrom, lineNo, id) {
    // Send info here
    send(JSON.stringify({
             messageType: "VARIABLE_WRITE",
             lineNo: lineNo,
             value: Object.prototype.toString.call(newValue),
             variable: varName,
             alias: readFrom,
             order: counter++,
             globalID: id
    }));
    return newValue;
}

function _dynoWriteAug(varName, newValue, readFrom, lineNo, id) {
    // Augmented assginment
    // Send info here
    send(JSON.stringify({
             messageType: "VARIABLE_WRITE_ADDSUB",
             lineNo: lineNo,
             value: Object.prototype.toString.call(newValue),
             variable: varName,
             alias: readFrom,
             order: counter++,
             globalID: id
    }));
    return newValue;
}

function _dynoWriteReturnValue(varName, returnValue, lineNo, id) {
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

function _dynoWriteArg(varName, newValue, functionName, argNum, lineNo, id) {
    // Send info here
    send(JSON.stringify({
             messageType: "WRITE_AS_ARGUMENT",
             lineNo: lineNo,
             value: Object.prototype.toString.call(newValue),
             variable: varName,
             functionName: functionName,
             argumentNumber: argNum,
             order: counter++,
             globalID: id
    }));
    return newValue;
}
