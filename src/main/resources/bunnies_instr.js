var level = _dynoWrite("level", 0, 1);
function initBunnies() {
  var stage = _dynoWrite("stage", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("stage"), 8);
  _dynoRead('stage', stage, 9)[_dynoReadProp("onmousedown", 9)] = _dynoWrite("stage.onmousedown", _dynoRead('clickStage', clickStage, 0), 9);
  _dynoRead('stage', stage, 10)[_dynoReadProp("onclick", 10)] = _dynoWrite("stage.onclick", function() {
  return false;
}, _dynoRead('anonymous10', anonymous10, 2), 10);
  _dynoRead('stage', stage, 14)[_dynoReadProp("onmouseup", 14)] = _dynoWrite("stage.onmouseup", function() {
  return false;
}, _dynoRead('anonymous14', anonymous14, 2), 14);
  _dynoRead('stage', stage, 18)[_dynoReadProp("onmousemove", 18)] = _dynoWrite("stage.onmousemove", function() {
  return false;
}, _dynoRead('anonymous18', anonymous18, 2), 18);
  _dynoRead('document', document, 23)[_dynoReadProp("onmousedown", 23)] = _dynoWrite("document.onmousedown", function() {
  return false;
}, _dynoRead('anonymous23', anonymous23, 2), 23);
  _dynoFunc(levelUp, 'levelUp', 29)();
  return true;
}

function reinitialise() {
  var stage = _dynoWrite("stage", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("stage"), 38);
  _dynoFunc(removeClass, 'removeClass', 40)(_dynoRead('stage', stage, 39), "gameOver");
  _dynoRead('level', level, 40) = _dynoWrite("level", 0, 40);
  for (var i = _dynoWrite("i", 1, 42); _dynoRead('i', i, 42) <= _dynoRead('bunnies', bunnies, 42); _dynoRead('i', i, 42)++) 
    {
      var bunny = _dynoWrite("bunny", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("bunny" + _dynoRead('i', i, 0)), 44);
      _dynoRead('bunny', bunny, 45)[_dynoReadProp("style", 45)][_dynoReadProp("marginRight", 45)] = _dynoWrite("bunny.style.marginRight", 0, 45);
      _dynoRead('bunny', bunny, 46)[_dynoReadProp("style", 46)][_dynoReadProp("marginBottom", 46)] = _dynoWrite("bunny.style.marginBottom", 0, 46);
      _dynoRead('bunny', bunny, 47)[_dynoReadProp("style", 47)][_dynoReadProp("marginLeft", 47)] = _dynoWrite("bunny.style.marginLeft", 0, 47);
      _dynoRead('bunny', bunny, 48)[_dynoReadProp("direction", 48)] = _dynoWrite("bunny.direction", "up", 48);
      _dynoRead('bunny', bunny, 49)[_dynoReadProp("dropped", 49)] = _dynoWrite("bunny.dropped", false, 49);
      _dynoRead('bunny', bunny, 50)[_dynoReadProp("target", 50)] = _dynoWrite("bunny.target", false, 50);
      _dynoFunc(removeClass, 'removeClass', 52)(_dynoRead('bunny', bunny, 51), "dead");
      _dynoFunc(removeClass, 'removeClass', 53)(_dynoRead('bunny', bunny, 52), "poison");
      _dynoFunc(clearTimeout, 'clearTimeout', 54)(_dynoRead('bunny', bunny, 53)[_dynoReadProp("timer", 53)]);
    }
  _dynoRead('document', document, 56)[_dynoReadProp("getElementById", 56)]("bomb1")[_dynoReadProp("style", 56)][_dynoReadProp("marginLeft", 56)] = _dynoWrite("document.getElementById('bomb1').style.marginLeft", "0px", 56);
  _dynoRead('document', document, 57)[_dynoReadProp("getElementById", 57)]("bomb2")[_dynoReadProp("style", 57)][_dynoReadProp("marginRight", 57)] = _dynoWrite("document.getElementById('bomb2').style.marginRight", "0px", 57);
  var lives = _dynoWrite("lives", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("lives"), 59);
  _dynoRead('lives', lives, 60)[_dynoReadProp("livesLost", 60)] = _dynoWrite("lives.livesLost", 0, 60);
  _dynoRead('lives', lives, 61)[_dynoReadProp("className", 61)] = _dynoWrite("lives.className", "", 61);
  var score = _dynoWrite("score", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("score"), 63);
  var scoreP = _dynoWrite("scoreP", _dynoRead('score', score, 0)[_dynoReadProp("getElementsByTagName", 0)]("p")[0], 64);
  _dynoRead('scoreP', scoreP, 65)[_dynoReadProp("firstChild", 65)][_dynoReadProp("nodeValue", 65)] = _dynoWrite("scoreP.firstChild.nodeValue", "0", 65);
  var levelContainer = _dynoWrite("levelContainer", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("level"), 67);
  var levelP = _dynoWrite("levelP", _dynoRead('levelContainer', levelContainer, 0)[_dynoReadProp("getElementsByTagName", 0)]("p")[0], 68);
  var levelBar = _dynoWrite("levelBar", _dynoRead('levelContainer', levelContainer, 0)[_dynoReadProp("getElementsByTagName", 0)]("div")[1], 69);
  _dynoRead('levelContainer', levelContainer, 70)[_dynoReadProp("currTime", 70)] = _dynoWrite("levelContainer.currTime", 0, 70);
  _dynoRead('levelP', levelP, 71)[_dynoReadProp("firstChild", 71)][_dynoReadProp("nodeValue", 71)] = _dynoWrite("levelP.firstChild.nodeValue", _dynoRead('level', level, 0), 71);
  _dynoRead('levelBar', levelBar, 72)[_dynoReadProp("style", 72)][_dynoReadProp("width", 72)] = _dynoWrite("levelBar.style.width", "0", 72);
  _dynoFunc(levelUp, 'levelUp', 75)();
  return true;
}

function endGame() {
  _dynoFunc(clearTimeout, 'clearTimeout', 85)(_dynoRead('randomTimer', randomTimer, 84));
  for (var i = _dynoWrite("i", 1, 86); _dynoRead('i', i, 86) <= _dynoRead('bunnies', bunnies, 86); _dynoRead('i', i, 86)++) 
    {
      _dynoFunc(clearTimeout, 'clearTimeout', 89)(_dynoRead('document', document, 88)[_dynoReadProp("getElementById", 88)]("bunny" + _dynoRead('i', i, 88))[_dynoReadProp("timer", 88)]);
    }
  var stage = _dynoWrite("stage", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("stage"), 91);
  _dynoFunc(addClass, 'addClass', 93)(_dynoRead('stage', stage, 92), "gameOver");
  var levelContainer = _dynoWrite("levelContainer", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("level"), 94);
  var levelMessage = _dynoWrite("levelMessage", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("levelMessage"), 95);
  _dynoFunc(clearTimeout, 'clearTimeout', 97)(_dynoRead('levelContainer', levelContainer, 96)[_dynoReadProp("timer", 96)]);
  _dynoRead('levelMessage', levelMessage, 97)[_dynoReadProp("innerHTML", 97)] = _dynoWrite("levelMessage.innerHTML", "", 97);
  var message = _dynoWrite("message", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("message"), 99);
  var score = _dynoWrite("score", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("score"), 100);
  var scoreP = _dynoWrite("scoreP", _dynoRead('score', score, 0)[_dynoReadProp("getElementsByTagName", 0)]("p")[0], 101);
  var randomMessage = _dynoWrite("randomMessage", _dynoRead('Math', Math, 0)[_dynoReadProp("ceil", 0)](_dynoRead('Math', Math, 0)[_dynoReadProp("random", 0)]() * 5), 102);
  var verb = _dynoWrite("verb", "splattered", 103);
  switch (_dynoRead('randomMessage', randomMessage, 105)) {
    case 1:
      _dynoRead('verb', verb, 109) = _dynoWrite("verb", "eviscerated", 109);
      break;
    case 2:
      _dynoRead('verb', verb, 115) = _dynoWrite("verb", "mutilated", 115);
      break;
    case 3:
      _dynoRead('verb', verb, 121) = _dynoWrite("verb", "massacred", 121);
      break;
    case 4:
      _dynoRead('verb', verb, 127) = _dynoWrite("verb", "butchered", 127);
      break;
  }
  _dynoRead('message', message, 132)[_dynoReadProp("innerHTML", 132)] = "Number of bunnies " + _dynoRead('verb', verb, 0) + ": <p>" + _dynoRead('scoreP', scoreP, 0)[_dynoReadProp("firstChild", 0)][_dynoReadProp("nodeValue", 0)] + "</p>";
  var buttonReload = _dynoWrite("buttonReload", _dynoRead('document', document, 0)[_dynoReadProp("createElement", 0)]("input"), 134);
  _dynoRead('buttonReload', buttonReload, 135)[_dynoReadProp("type", 135)] = _dynoWrite("buttonReload.type", "image", 135);
  _dynoRead('buttonReload', buttonReload, 136)[_dynoReadProp("src", 136)] = _dynoWrite("buttonReload.src", "images/button_reload.gif", 136);
  _dynoRead('buttonReload', buttonReload, 137)[_dynoReadProp("onclick", 137)] = _dynoWrite("buttonReload.onclick", _dynoRead('reinitialise', reinitialise, 0), 137);
  _dynoRead('message', message, 138)[_dynoReadProp("appendChild", 138)](_dynoRead('buttonReload', buttonReload, 138));
  var buttonStop = _dynoWrite("buttonStop", _dynoRead('document', document, 0)[_dynoReadProp("createElement", 0)]("input"), 140);
  _dynoRead('buttonStop', buttonStop, 141)[_dynoReadProp("type", 141)] = _dynoWrite("buttonStop.type", "image", 141);
  _dynoRead('buttonStop', buttonStop, 142)[_dynoReadProp("src", 142)] = _dynoWrite("buttonStop.src", "images/button_stop.gif", 142);
  _dynoRead('buttonStop', buttonStop, 143)[_dynoReadProp("onclick", 143)] = _dynoWrite("buttonStop.onclick", _dynoRead('stop', stop, 0), 143);
  _dynoRead('message', message, 144)[_dynoReadProp("appendChild", 144)](_dynoRead('buttonStop', buttonStop, 144));
  return true;
}

function stop() {
  var closing = _dynoWrite("closing", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("closing"), 154);
  _dynoRead('closing', closing, 155)[_dynoReadProp("className", 155)] = _dynoWrite("closing.className", "ready", 155);
  var stage = _dynoWrite("stage", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("stage"), 157);
  _dynoRead('stage', stage, 158)[_dynoReadProp("className", 158)] = _dynoWrite("stage.className", "", 158);
  return true;
}

function levelTimer() {
  var levelLength = _dynoWrite("levelLength", 12, 168);
  var levelContainer = _dynoWrite("levelContainer", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("level"), 169);
  var levelP = _dynoWrite("levelP", _dynoRead('levelContainer', levelContainer, 0)[_dynoReadProp("getElementsByTagName", 0)]("p")[0], 170);
  var levelBar = _dynoWrite("levelBar", _dynoRead('levelContainer', levelContainer, 0)[_dynoReadProp("getElementsByTagName", 0)]("div")[1], 171);
  if (typeof _dynoRead('levelContainer', levelContainer, 173)[_dynoReadProp("currTime", 173)] == "undefined") 
  {
    _dynoRead('levelContainer', levelContainer, 175)[_dynoReadProp("currTime", 175)] = _dynoWrite("levelContainer.currTime", 0, 175);
  }
  _dynoRead('levelContainer', levelContainer, 178)[_dynoReadProp("currTime", 178)]++;
  if (_dynoRead('levelContainer', levelContainer, 180)[_dynoReadProp("currTime", 180)] >= _dynoRead('levelLength', levelLength, 180)) 
  {
    _dynoRead('levelBar', levelBar, 182)[_dynoReadProp("style", 182)][_dynoReadProp("width", 182)] = _dynoWrite("levelBar.style.width", "100%", 182);
    if (typeof _dynoRead('randomTimer', randomTimer, 184) != "undefined") 
    {
      _dynoFunc(clearTimeout, 'clearTimeout', 187)(_dynoRead('randomTimer', randomTimer, 186));
    }
    _dynoRead('randomTimer', randomTimer, 189) = _dynoWriteReturnValue("randomTimer", _dynoFunc(setTimeout, 'setTimeout', 1)("levelUp()", 1000), 189);
    return true;
  }
  _dynoRead('levelBar', levelBar, 194)[_dynoReadProp("style", 194)][_dynoReadProp("width", 194)] = _dynoRead('Math', Math, 0)[_dynoReadProp("ceil", 0)](_dynoRead('levelContainer', levelContainer, 0)[_dynoReadProp("currTime", 0)] / _dynoRead('levelLength', levelLength, 0) * 100) + "%";
  _dynoRead('levelContainer', levelContainer, 196)[_dynoReadProp("timer", 196)] = _dynoWriteReturnValue("levelContainer.timer", _dynoFunc(setTimeout, 'setTimeout', 1)("levelTimer()", 1000), 196);
  return true;
}

function levelUp() {
  var levelContainer = _dynoWrite("levelContainer", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("level"), 206);
  var levelP = _dynoWrite("levelP", _dynoRead('levelContainer', levelContainer, 0)[_dynoReadProp("getElementsByTagName", 0)]("p")[0], 207);
  var levelMessage = _dynoWrite("levelMessage", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("levelMessage"), 208);
  var levelBar = _dynoWrite("levelBar", _dynoRead('levelContainer', levelContainer, 0)[_dynoReadProp("getElementsByTagName", 0)]("div")[1], 209);
  _dynoRead('level', level, 211)++;
  _dynoRead('levelContainer', levelContainer, 212)[_dynoReadProp("currTime", 212)] = _dynoWrite("levelContainer.currTime", 0, 212);
  _dynoRead('levelP', levelP, 213)[_dynoReadProp("firstChild", 213)][_dynoReadProp("nodeValue", 213)] = _dynoWrite("levelP.firstChild.nodeValue", _dynoRead('level', level, 0), 213);
  _dynoRead('levelBar', levelBar, 214)[_dynoReadProp("style", 214)][_dynoReadProp("width", 214)] = _dynoWrite("levelBar.style.width", "0", 214);
  if (_dynoRead('level', level, 216) == 4) 
  {
    _dynoRead('bunnies', bunnies, 218) = _dynoWrite("bunnies", 10, 218);
  } else if (_dynoRead('level', level, 220) > 4) 
  {
    _dynoRead('bunnies', bunnies, 222) = _dynoWrite("bunnies", 11, 222);
  } else {
    _dynoRead('bunnies', bunnies, 226) = _dynoWrite("bunnies", 9, 226);
  }
  _dynoRead('levelMessage', levelMessage, 229)[_dynoReadProp("innerHTML", 229)] = "Level " + _dynoRead('level', level, 0);
  _dynoRead('levelMessage', levelMessage, 230)[_dynoReadProp("style", 230)][_dynoReadProp("display", 230)] = _dynoWrite("levelMessage.style.display", "block", 230);
  _dynoRead('randomTimer', randomTimer, 232) = _dynoWriteReturnValue("randomTimer", _dynoFunc(setTimeout, 'setTimeout', 1)("resumeLevel()", 1000), 232);
  return true;
}

function resumeLevel() {
  var increment = _dynoWrite("increment", 0.1, 242);
  var levelMessage = _dynoWrite("levelMessage", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("levelMessage"), 243);
  var opacity = _dynoWrite("opacity", _dynoFunc(parseFloat, 'parseFloat', 1)(_dynoRead('levelMessage', levelMessage, 0)[_dynoReadProp("style", 0)][_dynoReadProp("opacity", 0)]), 244);
  if (typeof _dynoRead('opacity', opacity, 246) == "undefined" || _dynoFunc(isNaN, 'isNaN', 247)(_dynoRead('opacity', opacity, 246)) || _dynoRead('opacity', opacity, 246) == "") 
  {
    _dynoRead('opacity', opacity, 248) = _dynoWrite("opacity", "1", 248);
  }
  if (_dynoRead('opacity', opacity, 251) <= _dynoRead('increment', increment, 251) * 1.5) 
  {
    _dynoRead('levelMessage', levelMessage, 253)[_dynoReadProp("style", 253)][_dynoReadProp("display", 253)] = _dynoWrite("levelMessage.style.display", "none", 253);
    _dynoRead('levelMessage', levelMessage, 254)[_dynoReadProp("style", 254)][_dynoReadProp("opacity", 254)] = _dynoWrite("levelMessage.style.opacity", "0.99", 254);
    _dynoFunc(levelTimer, 'levelTimer', 257)();
    _dynoFunc(randomBunny, 'randomBunny', 259)();
  } else {
    _dynoRead('levelMessage', levelMessage, 262)[_dynoReadProp("style", 262)][_dynoReadProp("opacity", 262)] = _dynoWrite("levelMessage.style.opacity", _dynoRead('opacity', opacity, 0) - _dynoRead('increment', increment, 0), 262);
    _dynoFunc(setTimeout, 'setTimeout', 264)("resumeLevel()", 25);
  }
  return true;
}

function lostLife() {
  var lives = _dynoWrite("lives", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("lives"), 274);
  if (typeof _dynoRead('lives', lives, 276)[_dynoReadProp("livesLost", 276)] == "undefined") 
  {
    _dynoRead('lives', lives, 278)[_dynoReadProp("livesLost", 278)] = _dynoWrite("lives.livesLost", 1, 278);
  } else {
    _dynoRead('lives', lives, 282)[_dynoReadProp("livesLost", 282)]++;
  }
  _dynoFunc(addClass, 'addClass', 286)(_dynoRead('lives', lives, 285), "lost" + _dynoRead('lives', lives, 285)[_dynoReadProp("livesLost", 285)]);
  if (_dynoRead('lives', lives, 287)[_dynoReadProp("livesLost", 287)] >= 3) 
  {
    _dynoFunc(endGame, 'endGame', 290)();
    return false;
  }
}

function randomBunny() {
  for (var i = _dynoWrite("i", 0, 300); _dynoRead('i', i, 300) < _dynoRead('level', level, 300) && _dynoRead('i', i, 300) < 3; _dynoRead('i', i, 300)++) 
    {
      var random = _dynoWrite("random", _dynoRead('Math', Math, 0)[_dynoReadProp("ceil", 0)](_dynoRead('Math', Math, 0)[_dynoReadProp("random", 0)]() * _dynoRead('bunnies', bunnies, 0)), 302);
      var bunny = _dynoWrite("bunny", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("bunny" + _dynoRead('random', random, 0)), 303);
      if (_dynoRead('bunny', bunny, 305)[_dynoReadProp("target", 305)] != true && !(_dynoRead('level', level, 305) < 6 && _dynoRead('random', random, 305) == 10 && _dynoRead('document', document, 305)[_dynoReadProp("getElementById", 305)]("bunny11")[_dynoReadProp("target", 305)] == true) && !(_dynoRead('level', level, 305) < 6 && _dynoRead('random', random, 305) == 11 && _dynoRead('document', document, 305)[_dynoReadProp("getElementById", 305)]("bunny10")[_dynoReadProp("target", 305)] == true)) 
      {
        _dynoRead('bunny', bunny, 307)[_dynoReadProp("target", 307)] = _dynoWrite("bunny.target", true, 307);
        if (_dynoRead('random', random, 309) == 10) 
        {
          _dynoFunc(bunnyJump2, 'bunnyJump2', 312)();
        } else if (_dynoRead('random', random, 313) == 11) 
        {
          _dynoFunc(bunnyJump3, 'bunnyJump3', 316)();
        } else {
          _dynoFunc(bunnyJump1, 'bunnyJump1', 320)(_dynoRead('bunny', bunny, 319));
          var deathRandom = _dynoWrite("deathRandom", _dynoRead('Math', Math, 0)[_dynoReadProp("random", 0)](), 321);
          if (_dynoRead('deathRandom', deathRandom, 323) > 0.85) 
          {
            _dynoFunc(addClass, 'addClass', 326)(_dynoRead('bunny', bunny, 325), "poison");
          }
        }
      } else {
        for (var j = _dynoWrite("j", 1, 331); _dynoRead('j', j, 331) <= _dynoRead('bunnies', bunnies, 331); _dynoRead('j', j, 331)++) 
          {
            if (_dynoRead('document', document, 333)[_dynoReadProp("getElementById", 333)]("bunny" + _dynoRead('j', j, 333))[_dynoReadProp("target", 333)] != true) 
            {
              break;
            }
          }
        if (_dynoRead('j', j, 339) > _dynoRead('bunnies', bunnies, 339)) 
        {
          break;
        } else {
          --_dynoRead('i', i, 345);
        }
      }
    }
  _dynoRead('randomTimer', randomTimer, 350) = _dynoWriteReturnValue("randomTimer", _dynoFunc(setTimeout, 'setTimeout', 1)("randomBunny()", 2000 - _dynoRead('level', level, 0) * 100), 350);
  return true;
}

function bunnyJump1(bunny) {
  var increment = _dynoWrite("increment", 7, 360);
  var interval = _dynoWrite("interval", 25, 361);
  var jumpHeight = _dynoWrite("jumpHeight", 130, 362);
  var marginBottom = _dynoWrite("marginBottom", _dynoFunc(parseInt, 'parseInt', 1)(_dynoRead('bunny', bunny, 0)[_dynoReadProp("style", 0)][_dynoReadProp("marginBottom", 0)]), 363);
  if (_dynoFunc(isNaN, 'isNaN', 366)(_dynoRead('marginBottom', marginBottom, 365))) 
  {
    _dynoRead('marginBottom', marginBottom, 367) = _dynoWrite("marginBottom", 0, 367);
  }
  if (_dynoRead('bunny', bunny, 370)[_dynoReadProp("direction", 370)] == "down" && _dynoRead('marginBottom', marginBottom, 370) < _dynoRead('increment', increment, 370)) 
  {
    _dynoRead('bunny', bunny, 372)[_dynoReadProp("direction", 372)] = _dynoWrite("bunny.direction", "up", 372);
    _dynoRead('bunny', bunny, 373)[_dynoReadProp("style", 373)][_dynoReadProp("marginBottom", 373)] = _dynoWrite("bunny.style.marginBottom", "0px", 373);
    _dynoRead('bunny', bunny, 374)[_dynoReadProp("target", 374)] = _dynoWrite("bunny.target", false, 374);
    _dynoFunc(removeClass, 'removeClass', 376)(_dynoRead('bunny', bunny, 375), "poison");
    return true;
  }
  if (_dynoRead('bunny', bunny, 380)[_dynoReadProp("direction", 380)] == "down") 
  {
    _dynoRead('bunny', bunny, 382)[_dynoReadProp("style", 382)][_dynoReadProp("marginBottom", 382)] = _dynoRead('marginBottom', marginBottom, 0) - 2 * _dynoRead('increment', increment, 0) + "px";
  } else {
    _dynoRead('bunny', bunny, 386)[_dynoReadProp("style", 386)][_dynoReadProp("marginBottom", 386)] = _dynoRead('marginBottom', marginBottom, 0) + _dynoRead('increment', increment, 0) + "px";
    if (_dynoFunc(parseInt, 'parseInt', 389)(_dynoRead('bunny', bunny, 388)[_dynoReadProp("style", 388)][_dynoReadProp("marginBottom", 388)]) > _dynoRead('jumpHeight', jumpHeight, 388)) 
    {
      _dynoRead('bunny', bunny, 390)[_dynoReadProp("direction", 390)] = _dynoWrite("bunny.direction", "down", 390);
      _dynoRead('interval', interval, 391) = 925 - (_dynoRead('level', level, 0) * 100);
    }
  }
  _dynoRead('bunny', bunny, 395)[_dynoReadProp("timer", 395)] = _dynoWriteReturnValue("bunny.timer", _dynoFunc(setTimeout, 'setTimeout', 1)(function() {
  _dynoFunc(bunnyJump1, 'bunnyJump1', 2)(_dynoRead('bunny', bunny, 1));
}, _dynoRead('interval', interval, 2)), 395);
  return true;
}

function bunnyJump2(bunny) {
  var bunny = _dynoWrite("bunny", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("bunny10"), 408);
  var bomb = _dynoWrite("bomb", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("bomb1"), 409);
  var increment = _dynoWrite("increment", 7, 410);
  var interval = _dynoWrite("interval", 25, 411);
  var jumpHeight = _dynoWrite("jumpHeight", 90, 412);
  var marginLeft = _dynoWrite("marginLeft", _dynoFunc(parseInt, 'parseInt', 1)(_dynoRead('bunny', bunny, 0)[_dynoReadProp("style", 0)][_dynoReadProp("marginLeft", 0)]), 413);
  if (_dynoFunc(isNaN, 'isNaN', 416)(_dynoRead('marginLeft', marginLeft, 415))) 
  {
    _dynoRead('marginLeft', marginLeft, 417) = _dynoWrite("marginLeft", 0, 417);
  }
  if (_dynoRead('marginLeft', marginLeft, 420) > _dynoRead('jumpHeight', jumpHeight, 420) && _dynoRead('bunny', bunny, 420)[_dynoReadProp("direction", 420)] == "down") 
  {
    _dynoRead('bunny', bunny, 422)[_dynoReadProp("dropped", 422)] = _dynoWrite("bunny.dropped", true, 422);
    _dynoFunc(dropBomb, 'dropBomb', 424)(_dynoRead('bomb', bomb, 423));
  }
  if (_dynoRead('bunny', bunny, 426)[_dynoReadProp("direction", 426)] == "down" && _dynoRead('marginLeft', marginLeft, 426) < _dynoRead('increment', increment, 426)) 
  {
    _dynoRead('bunny', bunny, 428)[_dynoReadProp("direction", 428)] = _dynoWrite("bunny.direction", "up", 428);
    _dynoRead('bunny', bunny, 429)[_dynoReadProp("style", 429)][_dynoReadProp("marginLeft", 429)] = _dynoWrite("bunny.style.marginLeft", "0px", 429);
    _dynoRead('bunny', bunny, 430)[_dynoReadProp("dropped", 430)] = _dynoWrite("bunny.dropped", false, 430);
    return true;
  }
  if (_dynoRead('bunny', bunny, 435)[_dynoReadProp("direction", 435)] == "down") 
  {
    _dynoRead('bunny', bunny, 437)[_dynoReadProp("style", 437)][_dynoReadProp("marginLeft", 437)] = _dynoRead('marginLeft', marginLeft, 0) - 2 * _dynoRead('increment', increment, 0) + "px";
  } else {
    _dynoRead('bunny', bunny, 441)[_dynoReadProp("style", 441)][_dynoReadProp("marginLeft", 441)] = _dynoRead('marginLeft', marginLeft, 0) + _dynoRead('increment', increment, 0) + "px";
    _dynoRead('bomb', bomb, 442)[_dynoReadProp("style", 442)][_dynoReadProp("marginLeft", 442)] = _dynoRead('marginLeft', marginLeft, 0) + _dynoRead('increment', increment, 0) + "px";
    if (_dynoFunc(parseInt, 'parseInt', 445)(_dynoRead('bunny', bunny, 444)[_dynoReadProp("style", 444)][_dynoReadProp("marginLeft", 444)]) > _dynoRead('jumpHeight', jumpHeight, 444)) 
    {
      _dynoRead('bunny', bunny, 446)[_dynoReadProp("direction", 446)] = _dynoWrite("bunny.direction", "down", 446);
      _dynoRead('interval', interval, 447) = 1325 - (_dynoRead('level', level, 0) * 50);
    }
  }
  _dynoRead('bunny', bunny, 451)[_dynoReadProp("timer", 451)] = _dynoWriteReturnValue("bunny.timer", _dynoFunc(setTimeout, 'setTimeout', 1)(function() {
  _dynoFunc(bunnyJump2, 'bunnyJump2', 2)(_dynoRead('bunny', bunny, 1));
}, _dynoRead('interval', interval, 2)), 451);
  return true;
}

function bunnyJump3(bunny) {
  var bunny = _dynoWrite("bunny", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("bunny11"), 464);
  var bomb = _dynoWrite("bomb", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("bomb2"), 465);
  var increment = _dynoWrite("increment", 7, 466);
  var interval = _dynoWrite("interval", 25, 467);
  var jumpHeight = _dynoWrite("jumpHeight", 95, 468);
  var marginRight = _dynoWrite("marginRight", _dynoFunc(parseInt, 'parseInt', 1)(_dynoRead('bunny', bunny, 0)[_dynoReadProp("style", 0)][_dynoReadProp("marginRight", 0)]), 469);
  if (_dynoFunc(isNaN, 'isNaN', 472)(_dynoRead('marginRight', marginRight, 471))) 
  {
    _dynoRead('marginRight', marginRight, 473) = _dynoWrite("marginRight", 0, 473);
  }
  if (_dynoRead('marginRight', marginRight, 476) > _dynoRead('jumpHeight', jumpHeight, 476) && _dynoRead('bunny', bunny, 476)[_dynoReadProp("direction", 476)] == "down") 
  {
    _dynoRead('bunny', bunny, 478)[_dynoReadProp("dropped", 478)] = _dynoWrite("bunny.dropped", true, 478);
    _dynoFunc(dropBomb, 'dropBomb', 480)(_dynoRead('bomb', bomb, 479));
  }
  if (_dynoRead('bunny', bunny, 482)[_dynoReadProp("direction", 482)] == "down" && _dynoRead('marginRight', marginRight, 482) < _dynoRead('increment', increment, 482)) 
  {
    _dynoRead('bunny', bunny, 484)[_dynoReadProp("direction", 484)] = _dynoWrite("bunny.direction", "up", 484);
    _dynoRead('bunny', bunny, 485)[_dynoReadProp("style", 485)][_dynoReadProp("marginRight", 485)] = _dynoWrite("bunny.style.marginRight", "0px", 485);
    _dynoRead('bunny', bunny, 486)[_dynoReadProp("dropped", 486)] = _dynoWrite("bunny.dropped", false, 486);
    return true;
  }
  if (_dynoRead('bunny', bunny, 491)[_dynoReadProp("direction", 491)] == "down") 
  {
    _dynoRead('bunny', bunny, 493)[_dynoReadProp("style", 493)][_dynoReadProp("marginRight", 493)] = _dynoRead('marginRight', marginRight, 0) - 2 * _dynoRead('increment', increment, 0) + "px";
  } else {
    _dynoRead('bunny', bunny, 497)[_dynoReadProp("style", 497)][_dynoReadProp("marginRight", 497)] = _dynoRead('marginRight', marginRight, 0) + _dynoRead('increment', increment, 0) + "px";
    _dynoRead('bomb', bomb, 498)[_dynoReadProp("style", 498)][_dynoReadProp("marginRight", 498)] = _dynoRead('marginRight', marginRight, 0) + _dynoRead('increment', increment, 0) + "px";
    if (_dynoFunc(parseInt, 'parseInt', 501)(_dynoRead('bunny', bunny, 500)[_dynoReadProp("style", 500)][_dynoReadProp("marginRight", 500)]) > _dynoRead('jumpHeight', jumpHeight, 500)) 
    {
      _dynoRead('bunny', bunny, 502)[_dynoReadProp("direction", 502)] = _dynoWrite("bunny.direction", "down", 502);
      _dynoRead('interval', interval, 503) = 1325 - (_dynoRead('level', level, 0) * 50);
    }
  }
  _dynoRead('bunny', bunny, 507)[_dynoReadProp("timer", 507)] = _dynoWriteReturnValue("bunny.timer", _dynoFunc(setTimeout, 'setTimeout', 1)(function() {
  _dynoFunc(bunnyJump3, 'bunnyJump3', 2)(_dynoRead('bunny', bunny, 1));
}, _dynoRead('interval', interval, 2)), 507);
  return true;
}

function dropBomb(bomb) {
  var increment = _dynoWrite("increment", 11, 520);
  var interval = _dynoWrite("interval", 25, 521);
  var jumpHeight = _dynoWrite("jumpHeight", 280, 522);
  var marginTop = _dynoWrite("marginTop", _dynoFunc(parseInt, 'parseInt', 1)(_dynoRead('bomb', bomb, 0)[_dynoReadProp("style", 0)][_dynoReadProp("marginTop", 0)]), 523);
  if (_dynoFunc(isNaN, 'isNaN', 526)(_dynoRead('marginTop', marginTop, 525))) 
  {
    _dynoRead('marginTop', marginTop, 527) = _dynoWrite("marginTop", 0, 527);
  }
  if (_dynoRead('marginTop', marginTop, 530) > _dynoRead('jumpHeight', jumpHeight, 530)) 
  {
    _dynoFunc(lostLife, 'lostLife', 533)();
    _dynoFunc(addClass, 'addClass', 535)(_dynoRead('bomb', bomb, 534), "exploded");
    _dynoRead('bomb', bomb, 536)[_dynoReadProp("style", 536)][_dynoReadProp("marginTop", 536)] = _dynoWrite("bomb.style.marginTop", "205px", 536);
    _dynoFunc(blink, 'blink', 539)(_dynoRead('bomb', bomb, 538));
    return true;
  }
  _dynoRead('bomb', bomb, 543)[_dynoReadProp("style", 543)][_dynoReadProp("marginTop", 543)] = _dynoRead('marginTop', marginTop, 0) + _dynoRead('increment', increment, 0) + "px";
  _dynoRead('bomb', bomb, 545)[_dynoReadProp("timer", 545)] = _dynoWriteReturnValue("bomb.timer", _dynoFunc(setTimeout, 'setTimeout', 1)(function() {
  _dynoFunc(dropBomb, 'dropBomb', 2)(_dynoRead('bomb', bomb, 1));
}, _dynoRead('interval', interval, 2)), 545);
  return true;
}

function clickStage(event) {
  if (_dynoFunc(hasClass, 'hasClass', 559)(this, "gameOver")) 
  {
    return false;
  }
  if (typeof _dynoRead('event', event, 563) == "undefined") 
  {
    _dynoRead('event', event, 565) = _dynoWrite("event", _dynoRead('window', window, 0)[_dynoReadProp("event", 0)], 565);
  }
  if (typeof _dynoRead('event', event, 568)[_dynoReadProp("clientX", 568)] != "undefined") 
  {
    var clickX = _dynoWrite("clickX", _dynoRead('event', event, 0)[_dynoReadProp("clientX", 0)], 570);
    var clickY = _dynoWrite("clickY", _dynoRead('event', event, 0)[_dynoReadProp("clientY", 0)], 571);
  } else {
    var clickX = _dynoWrite("clickX", _dynoRead('event', event, 0)[_dynoReadProp("offsetX", 0)], 575);
    var clickY = _dynoWrite("clickY", _dynoRead('event', event, 0)[_dynoReadProp("offsetY", 0)], 576);
  }
  for (var i = _dynoWrite("i", 1, 579); _dynoRead('i', i, 579) <= _dynoRead('bunnies', bunnies, 579); _dynoRead('i', i, 579)++) 
    {
      var bunny = _dynoWrite("bunny", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("bunny" + _dynoRead('i', i, 0)), 581);
      var position = _dynoWrite("position", _dynoFunc(getPosition, 'getPosition', 1)(_dynoRead('bunny', bunny, 0)), 582);
      var stagePosition = _dynoWrite("stagePosition", _dynoFunc(getPosition, 'getPosition', 1)(this), 583);
      if (_dynoRead('bunny', bunny, 585)[_dynoReadProp("target", 585)] == true && !_dynoFunc(hasClass, 'hasClass', 586)(_dynoRead('bunny', bunny, 585), "dead") && _dynoRead('bunny', bunny, 585)[_dynoReadProp("dropped", 585)] != true) 
      {
        if (_dynoRead('clickX', clickX, 587) >= _dynoRead('position', position, 587)[0] && _dynoRead('clickX', clickX, 587) <= _dynoRead('position', position, 587)[0] + _dynoRead('bunny', bunny, 587)[_dynoReadProp("offsetWidth", 587)]) 
        {
          if (_dynoRead('clickY', clickY, 589) >= _dynoRead('position', position, 589)[1] && _dynoRead('clickY', clickY, 589) <= _dynoRead('position', position, 589)[1] + _dynoRead('bunny', bunny, 589)[_dynoReadProp("offsetHeight", 589)]) 
          {
            _dynoFunc(clearTimeout, 'clearTimeout', 592)(_dynoRead('bunny', bunny, 591)[_dynoReadProp("timer", 591)]);
            _dynoFunc(addClass, 'addClass', 594)(_dynoRead('bunny', bunny, 593), "dead");
            if (_dynoFunc(hasClass, 'hasClass', 596)(_dynoRead('bunny', bunny, 595), "poison")) 
            {
              _dynoFunc(blink, 'blink', 598)(_dynoRead('bunny', bunny, 597));
              _dynoFunc(lostLife, 'lostLife', 600)();
            } else {
              _dynoFunc(setTimeout, 'setTimeout', 604)(function() {
  _dynoFunc(fade, 'fade', 606)(_dynoRead('bunny', bunny, 605));
}, 750);
              var score = _dynoWrite("score", _dynoRead('document', document, 0)[_dynoReadProp("getElementById", 0)]("score"), 608);
              var scoreP = _dynoWrite("scoreP", _dynoRead('score', score, 0)[_dynoReadProp("getElementsByTagName", 0)]("p")[0], 609);
              _dynoRead('scoreP', scoreP, 610)[_dynoReadProp("firstChild", 610)][_dynoReadProp("nodeValue", 610)] = _dynoFunc(parseInt, 'parseInt', 1)(_dynoRead('scoreP', scoreP, 0)[_dynoReadProp("firstChild", 0)][_dynoReadProp("nodeValue", 0)]) + 1;
            }
            break;
          }
        }
      }
    }
  return false;
}

function fade(bunny) {
  _dynoRead('bunny', bunny, 627)[_dynoReadProp("style", 627)][_dynoReadProp("marginRight", 627)] = _dynoWrite("bunny.style.marginRight", "0", 627);
  _dynoRead('bunny', bunny, 628)[_dynoReadProp("style", 628)][_dynoReadProp("marginBottom", 628)] = _dynoWrite("bunny.style.marginBottom", "0", 628);
  _dynoRead('bunny', bunny, 629)[_dynoReadProp("style", 629)][_dynoReadProp("marginLeft", 629)] = _dynoWrite("bunny.style.marginLeft", "0", 629);
  _dynoFunc(removeClass, 'removeClass', 631)(_dynoRead('bunny', bunny, 630), "dead");
  _dynoFunc(removeClass, 'removeClass', 632)(_dynoRead('bunny', bunny, 631), "poison");
  _dynoRead('bunny', bunny, 632)[_dynoReadProp("target", 632)] = _dynoWrite("bunny.target", false, 632);
  _dynoRead('bunny', bunny, 633)[_dynoReadProp("direction", 633)] = _dynoWrite("bunny.direction", "up", 633);
  if (_dynoRead('bunny', bunny, 635)[_dynoReadProp("id", 635)] == "bunny10") 
  {
    _dynoRead('document', document, 637)[_dynoReadProp("getElementById", 637)]("bomb1")[_dynoReadProp("style", 637)][_dynoReadProp("marginLeft", 637)] = _dynoWrite("document.getElementById('bomb1').style.marginLeft", "0px", 637);
  } else if (_dynoRead('bunny', bunny, 639)[_dynoReadProp("id", 639)] == "bunny11") 
  {
    _dynoRead('document', document, 641)[_dynoReadProp("getElementById", 641)]("bomb2")[_dynoReadProp("style", 641)][_dynoReadProp("marginRight", 641)] = _dynoWrite("document.getElementById('bomb2').style.marginRight", "0px", 641);
  }
  return true;
}

function blink(bunny) {
  var display = _dynoWrite("display", _dynoRead('bunny', bunny, 0)[_dynoReadProp("style", 0)][_dynoReadProp("display", 0)], 652);
  if (_dynoRead('display', display, 654) == "") 
  {
    _dynoRead('display', display, 656) = _dynoWrite("display", "block", 656);
  }
  if (_dynoRead('display', display, 659) == "none") 
  {
    _dynoRead('bunny', bunny, 661)[_dynoReadProp("style", 661)][_dynoReadProp("display", 661)] = _dynoWrite("bunny.style.display", "block", 661);
  } else {
    _dynoRead('bunny', bunny, 665)[_dynoReadProp("style", 665)][_dynoReadProp("display", 665)] = _dynoWrite("bunny.style.display", "none", 665);
  }
  if (typeof _dynoRead('bunny', bunny, 668)[_dynoReadProp("blinkCounter", 668)] == "undefined") 
  {
    _dynoRead('bunny', bunny, 670)[_dynoReadProp("blinkCounter", 670)] = _dynoWrite("bunny.blinkCounter", 0, 670);
  }
  if (_dynoRead('bunny', bunny, 673)[_dynoReadProp("blinkCounter", 673)] > 5) 
  {
    _dynoRead('bunny', bunny, 675)[_dynoReadProp("blinkCounter", 675)] = _dynoWrite("bunny.blinkCounter", 0, 675);
    _dynoRead('bunny', bunny, 676)[_dynoReadProp("style", 676)][_dynoReadProp("marginBottom", 676)] = _dynoWrite("bunny.style.marginBottom", "0", 676);
    _dynoRead('bunny', bunny, 677)[_dynoReadProp("style", 677)][_dynoReadProp("display", 677)] = _dynoWrite("bunny.style.display", "block", 677);
    _dynoFunc(removeClass, 'removeClass', 679)(_dynoRead('bunny', bunny, 678), "dead");
    _dynoFunc(removeClass, 'removeClass', 680)(_dynoRead('bunny', bunny, 679), "poison");
    if (_dynoRead('bunny', bunny, 681)[_dynoReadProp("id", 681)][_dynoReadProp("match", 681)](/bomb/)) 
    {
      _dynoRead('bunny', bunny, 683)[_dynoReadProp("style", 683)][_dynoReadProp("marginRight", 683)] = _dynoWrite("bunny.style.marginRight", "0px", 683);
      _dynoRead('bunny', bunny, 684)[_dynoReadProp("style", 684)][_dynoReadProp("marginLeft", 684)] = _dynoWrite("bunny.style.marginLeft", "0px", 684);
      _dynoRead('bunny', bunny, 685)[_dynoReadProp("style", 685)][_dynoReadProp("marginTop", 685)] = _dynoWrite("bunny.style.marginTop", "0px", 685);
      if (_dynoRead('bunny', bunny, 687)[_dynoReadProp("id", 687)] == "bomb1") 
      {
        _dynoRead('document', document, 689)[_dynoReadProp("getElementById", 689)]("bunny10")[_dynoReadProp("target", 689)] = _dynoWrite("document.getElementById('bunny10').target", false, 689);
      } else {
        _dynoRead('document', document, 693)[_dynoReadProp("getElementById", 693)]("bunny11")[_dynoReadProp("target", 693)] = _dynoWrite("document.getElementById('bunny11').target", false, 693);
      }
      _dynoFunc(removeClass, 'removeClass', 697)(_dynoRead('bunny', bunny, 696), "exploded");
    } else {
      _dynoRead('bunny', bunny, 700)[_dynoReadProp("target", 700)] = _dynoWrite("bunny.target", false, 700);
      _dynoRead('bunny', bunny, 701)[_dynoReadProp("direction", 701)] = _dynoWrite("bunny.direction", "up", 701);
    }
  } else {
    _dynoRead('bunny', bunny, 706)[_dynoReadProp("blinkCounter", 706)]++;
    _dynoFunc(setTimeout, 'setTimeout', 709)(function() {
  _dynoFunc(blink, 'blink', 711)(_dynoRead('bunny', bunny, 710));
}, 500);
  }
  return true;
}

function getPosition(theElement) {
  var positionX = _dynoWrite("positionX", 0, 722);
  var positionY = _dynoWrite("positionY", 0, 723);
  while (_dynoRead('theElement', theElement, 725) != null) 
    {
      _dynoRead('positionX', positionX, 727) += _dynoRead('theElement', theElement, 727)[_dynoReadProp("offsetLeft", 727)];
      _dynoRead('positionY', positionY, 728) += _dynoRead('theElement', theElement, 728)[_dynoReadProp("offsetTop", 728)];
      _dynoRead('theElement', theElement, 729) = _dynoWrite("theElement", _dynoRead('theElement', theElement, 0)[_dynoReadProp("offsetParent", 0)], 729);
    }
  return [_dynoRead('positionX', positionX, 732), _dynoRead('positionY', positionY, 732)];
}
