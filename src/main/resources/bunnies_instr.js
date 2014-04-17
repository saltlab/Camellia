var level = 0;
function initBunnies() {
  var stage = document.getElementById('stage');
  stage.onmousedown = clickStage;
  stage.onclick = function() {
  return false;
};
  stage.onmouseup = function() {
  return false;
};
  stage.onmousemove = function() {
  return false;
};
  document.onmousedown = function() {
  return false;
};
  levelUp();
  return true;
}
function reinitialise() {
  var stage = document.getElementById('stage');
  removeClass(stage, "gameOver");
  level = 0;
  for (var i = 1; i <= bunnies; i++) 
    {
      var bunny = document.getElementById('bunny' + i);
      bunny.style.marginRight = 0;
      bunny.style.marginBottom = 0;
      bunny.style.marginLeft = 0;
      bunny.direction = "up";
      bunny.dropped = false;
      bunny.target = false;
      removeClass(bunny, "dead");
      removeClass(bunny, "poison");
      clearTimeout(bunny.timer);
    }
  document.getElementById("bomb1").style.marginLeft = "0px";
  document.getElementById("bomb2").style.marginRight = "0px";
  var lives = document.getElementById('lives');
  lives.livesLost = 0;
  lives.className = "";
  var score = document.getElementById('score');
  var scoreP = score.getElementsByTagName('p')[0];
  scoreP.firstChild.nodeValue = "0";
  var levelContainer = document.getElementById('level');
  var levelP = levelContainer.getElementsByTagName('p')[0];
  var levelBar = levelContainer.getElementsByTagName('div')[1];
  levelContainer.currTime = 0;
  levelP.firstChild.nodeValue = level;
  levelBar.style.width = "0";
  levelUp();
  return true;
}
function endGame() {
  clearTimeout(randomTimer);
  for (var i = 1; i <= bunnies; i++) 
    {
      clearTimeout(document.getElementById("bunny" + i).timer);
    }
  var stage = document.getElementById('stage');
  addClass(stage, "gameOver");
  var levelContainer = document.getElementById('level');
  var levelMessage = document.getElementById('levelMessage');
  clearTimeout(levelContainer.timer);
  levelMessage.innerHTML = "";
  var message = document.getElementById('message');
  var score = document.getElementById('score');
  var scoreP = score.getElementsByTagName('p')[0];
  var randomMessage = Math.ceil(Math.random() * 5);
  var verb = 'splattered';
  switch (randomMessage) {
    case 1:
      verb = "eviscerated";
      break;
    case 2:
      verb = "mutilated";
      break;
    case 3:
      verb = "massacred";
      break;
    case 4:
      verb = "butchered";
      break;
  }
  message.innerHTML = "Number of bunnies " + verb + ": <p>" + scoreP.firstChild.nodeValue + "</p>";
  var buttonReload = document.createElement('input');
  buttonReload.type = "image";
  buttonReload.src = "images/button_reload.gif";
  buttonReload.onclick = reinitialise;
  message.appendChild(buttonReload);
  var buttonStop = document.createElement('input');
  buttonStop.type = "image";
  buttonStop.src = "images/button_stop.gif";
  buttonStop.onclick = stop;
  message.appendChild(buttonStop);
  return true;
}
function stop() {
  var closing = document.getElementById('closing');
  closing.className = "ready";
  var stage = document.getElementById('stage');
  stage.className = "";
  return true;
}
function levelTimer() {
  var levelLength = 12;
  var levelContainer = document.getElementById('level');
  var levelP = levelContainer.getElementsByTagName('p')[0];
  var levelBar = levelContainer.getElementsByTagName('div')[1];
  if (typeof levelContainer.currTime == "undefined") 
  {
    levelContainer.currTime = 0;
  }
  levelContainer.currTime++;
  if (levelContainer.currTime >= levelLength) 
  {
    levelBar.style.width = "100%";
    if (typeof randomTimer != "undefined") 
    {
      clearTimeout(randomTimer);
    }
    randomTimer = setTimeout("levelUp()", 1000);
    return true;
  }
  levelBar.style.width = Math.ceil(levelContainer.currTime / levelLength * 100) + "%";
  levelContainer.timer = setTimeout("levelTimer()", 1000);
  return true;
}
function levelUp() {
  var levelContainer = document.getElementById('level');
  var levelP = levelContainer.getElementsByTagName('p')[0];
  var levelMessage = document.getElementById('levelMessage');
  var levelBar = levelContainer.getElementsByTagName('div')[1];
  level++;
  levelContainer.currTime = 0;
  levelP.firstChild.nodeValue = level;
  levelBar.style.width = "0";
  if (level == 4) 
  {
    bunnies = 10;
  } else if (level > 4) 
  {
    bunnies = 11;
  } else {
    bunnies = 9;
  }
  levelMessage.innerHTML = "Level " + level;
  levelMessage.style.display = "block";
  randomTimer = setTimeout("resumeLevel()", 1000);
  return true;
}
function resumeLevel() {
  var increment = 0.1;
  var levelMessage = document.getElementById('levelMessage');
  var opacity = parseFloat(levelMessage.style.opacity);
  if (typeof opacity == "undefined" || isNaN(opacity) || opacity == "") 
  {
    opacity = "1";
  }
  if (opacity <= increment * 1.5) 
  {
    levelMessage.style.display = "none";
    levelMessage.style.opacity = "0.99";
    levelTimer();
    randomBunny();
  } else {
    levelMessage.style.opacity = opacity - increment;
    setTimeout("resumeLevel()", 25);
  }
  return true;
}
function lostLife() {
  var lives = document.getElementById('lives');
  if (typeof lives.livesLost == "undefined") 
  {
    lives.livesLost = 1;
  } else {
    lives.livesLost++;
  }
  addClass(lives, "lost" + lives.livesLost);
  if (lives.livesLost >= 3) 
  {
    endGame();
    return false;
  }
}
function randomBunny() {
  for (var i = 0; i < level && i < 3; i++) 
    {
      var random = Math.ceil(Math.random() * bunnies);
      var bunny = document.getElementById('bunny' + random);
      if (bunny.target != true && !(level < 6 && random == 10 && document.getElementById("bunny11").target == true) && !(level < 6 && random == 11 && document.getElementById("bunny10").target == true)) 
      {
        bunny.target = true;
        if (random == 10) 
        {
          bunnyJump2();
        } else if (random == 11) 
        {
          bunnyJump3();
        } else {
          bunnyJump1(bunny);
          var deathRandom = Math.random();
          if (deathRandom > 0.85) 
          {
            addClass(bunny, "poison");
          }
        }
      } else {
        for (var j = 1; j <= bunnies; j++) 
          {
            if (document.getElementById("bunny" + j).target != true) 
            {
              break;
            }
          }
        if (j > bunnies) 
        {
          break;
        } else {
          --i;
        }
      }
    }
  randomTimer = setTimeout("randomBunny()", 2000 - level * 100);
  return true;
}
function bunnyJump1(bunny) {
  var increment = 7;
  var interval = 25;
  var jumpHeight = 130;
  var marginBottom = parseInt(bunny.style.marginBottom);
  if (isNaN(marginBottom)) 
  {
    marginBottom = 0;
  }
  if (bunny.direction == "down" && marginBottom < increment) 
  {
    bunny.direction = "up";
    bunny.style.marginBottom = "0px";
    bunny.target = false;
    removeClass(bunny, "poison");
    return true;
  }
  if (bunny.direction == "down") 
  {
    bunny.style.marginBottom = marginBottom - 2 * increment + "px";
  } else {
    bunny.style.marginBottom = marginBottom + increment + "px";
    if (parseInt(bunny.style.marginBottom) > jumpHeight) 
    {
      bunny.direction = "down";
      interval = 925 - (level * 100);
    }
  }
  bunny.timer = setTimeout(function() {
  bunnyJump1(bunny);
}, interval);
  return true;
}
function bunnyJump2(bunny) {
  var bunny = document.getElementById('bunny10');
  var bomb = document.getElementById('bomb1');
  var increment = 7;
  var interval = 25;
  var jumpHeight = 90;
  var marginLeft = parseInt(bunny.style.marginLeft);
  if (isNaN(marginLeft)) 
  {
    marginLeft = 0;
  }
  if (marginLeft > jumpHeight && bunny.direction == "down") 
  {
    bunny.dropped = true;
    dropBomb(bomb);
  }
  if (bunny.direction == "down" && marginLeft < increment) 
  {
    bunny.direction = "up";
    bunny.style.marginLeft = "0px";
    bunny.dropped = false;
    return true;
  }
  if (bunny.direction == "down") 
  {
    bunny.style.marginLeft = marginLeft - 2 * increment + "px";
  } else {
    bunny.style.marginLeft = marginLeft + increment + "px";
    bomb.style.marginLeft = marginLeft + increment + "px";
    if (parseInt(bunny.style.marginLeft) > jumpHeight) 
    {
      bunny.direction = "down";
      interval = 1325 - (level * 50);
    }
  }
  bunny.timer = setTimeout(function() {
  bunnyJump2(bunny);
}, interval);
  return true;
}
function bunnyJump3(bunny) {
  var bunny = document.getElementById('bunny11');
  var bomb = document.getElementById('bomb2');
  var increment = 7;
  var interval = 25;
  var jumpHeight = 95;
  var marginRight = parseInt(bunny.style.marginRight);
  if (isNaN(marginRight)) 
  {
    marginRight = 0;
  }
  if (marginRight > jumpHeight && bunny.direction == "down") 
  {
    bunny.dropped = true;
    dropBomb(bomb);
  }
  if (bunny.direction == "down" && marginRight < increment) 
  {
    bunny.direction = "up";
    bunny.style.marginRight = "0px";
    bunny.dropped = false;
    return true;
  }
  if (bunny.direction == "down") 
  {
    bunny.style.marginRight = marginRight - 2 * increment + "px";
  } else {
    bunny.style.marginRight = marginRight + increment + "px";
    bomb.style.marginRight = marginRight + increment + "px";
    if (parseInt(bunny.style.marginRight) > jumpHeight) 
    {
      bunny.direction = "down";
      interval = 1325 - (level * 50);
    }
  }
  bunny.timer = setTimeout(function() {
  bunnyJump3(bunny);
}, interval);
  return true;
}
function dropBomb(bomb) {
  var increment = 11;
  var interval = 25;
  var jumpHeight = 280;
  var marginTop = parseInt(bomb.style.marginTop);
  if (isNaN(marginTop)) 
  {
    marginTop = 0;
  }
  if (marginTop > jumpHeight) 
  {
    lostLife();
    addClass(bomb, "exploded");
    bomb.style.marginTop = "205px";
    blink(bomb);
    return true;
  }
  bomb.style.marginTop = marginTop + increment + "px";
  bomb.timer = setTimeout(function() {
  dropBomb(bomb);
}, interval);
  return true;
}
function clickStage(event) {
  if (hasClass(_dynoReadAsArg('this', this, 'hasClass', 0, 558), "gameOver")) 
  {
    return false;
  }
  if (typeof event == "undefined") 
  {
    event = window.event;
  }
  if (typeof event.clientX != "undefined") 
  {
    var clickX = event.clientX;
    var clickY = event.clientY;
  } else {
    var clickX = event.offsetX;
    var clickY = event.offsetY;
  }
  for (var i = _dynoWrite('i', 1, '', 579); _dynoRead('i', i, 579) <= bunnies; _dynoRead('i', i, 579)++) 
    {
      var bunny = _dynoWrite('bunny', document.getElementById('bunny' + _dynoRead('i', i, 581)), 'document.getElementById', 581);
      var position = getPosition(_dynoReadAsArg('bunny', bunny, "getPosition", 0, 582));
      var stagePosition = getPosition(_dynoReadAsArg('this', this, 'getPosition', 0, 583));
      if (_dynoRead('bunny', bunny, 585)[_dynoReadProp("bunny", "target", 585)] == true && !hasClass(_dynoReadAsArg('bunny', bunny, "hasClass", 0, 585), "dead") && _dynoRead('bunny', bunny, 585)[_dynoReadProp("bunny", "dropped", 585)] != true) 
      {
        if (clickX >= position[0] && clickX <= position[0] + _dynoRead('bunny', bunny, 587)[_dynoReadProp("bunny", "offsetWidth", 587)]) 
        {
          if (clickY >= position[1] && clickY <= position[1] + _dynoRead('bunny', bunny, 589)[_dynoReadProp("bunny", "offsetHeight", 589)]) 
          {
            clearTimeout(_dynoReadAsArg('bunny', _dynoRead('bunny', bunny, 591)[_dynoReadProp('bunny', 'timer', 591)], 'clearTimeout', 0, 591));
            addClass(_dynoReadAsArg('bunny', bunny, "addClass", 0, 593), "dead");
            if (hasClass(_dynoReadAsArg('bunny', bunny, "hasClass", 0, 595), "poison")) 
            {
              blink(_dynoReadAsArg('bunny', bunny, "blink", 0, 597));
              lostLife();
            } else {
              setTimeout(function() {
  fade(_dynoReadAsArg('bunny', bunny, "fade", 0, 605));
}, 750);
              var score = document.getElementById('score');
              var scoreP = score.getElementsByTagName('p')[0];
              scoreP.firstChild.nodeValue = parseInt(scoreP.firstChild.nodeValue) + 1;
            }
            break;
          }
        }
      }
    }
  return false;
}
function fade(bunny) {
  _dynoWriteArg('bunny', bunny, 'fade', 0, 625);
  bunny.style.marginRight = _dynoWrite('bunny.style.marginRight', '0', '', 627);
  bunny.style.marginBottom = _dynoWrite('bunny.style.marginBottom', '0', '', 628);
  bunny.style.marginLeft = _dynoWrite('bunny.style.marginLeft', '0', '', 629);
  removeClass(_dynoReadAsArg('bunny', bunny, "removeClass", 0, 630), "dead");
  removeClass(_dynoReadAsArg('bunny', bunny, "removeClass", 0, 631), "poison");
  bunny.target = _dynoWrite('bunny.target', false, '', 632);
  bunny.direction = _dynoWrite('bunny.direction', 'up', '', 633);
  if (_dynoRead('bunny', bunny, 635)[_dynoReadProp("bunny", "id", 635)] == "bunny10") 
  {
    document.getElementById("bomb1").style.marginLeft = "0px";
  } else if (_dynoRead('bunny', bunny, 639)[_dynoReadProp("bunny", "id", 639)] == "bunny11") 
  {
    document.getElementById("bomb2").style.marginRight = "0px";
  }
  return true;
}
function blink(bunny) {
  _dynoWriteArg('bunny', bunny, 'blink', 0, 650);
  var display = _dynoWrite('display', _dynoRead('bunny', bunny, 652)[_dynoReadProp("bunny", "style", 652)][_dynoReadProp("bunny.style", "display", 652)], 'bunny.style.display', 652);
  if (display == "") 
  {
    display = "block";
  }
  if (display == "none") 
  {
    bunny.style.display = _dynoWrite('bunny.style.display', 'block', '', 661);
  } else {
    bunny.style.display = _dynoWrite('bunny.style.display', 'none', '', 665);
  }
  if (typeof _dynoRead('bunny', bunny, 668)[_dynoReadProp("bunny", "blinkCounter", 668)] == "undefined") 
  {
    bunny.blinkCounter = _dynoWrite('bunny.blinkCounter', 0, '', 670);
  }
  if (_dynoRead('bunny', bunny, 673)[_dynoReadProp("bunny", "blinkCounter", 673)] > 5) 
  {
    bunny.blinkCounter = _dynoWrite('bunny.blinkCounter', 0, '', 675);
    bunny.style.marginBottom = _dynoWrite('bunny.style.marginBottom', '0', '', 676);
    bunny.style.display = _dynoWrite('bunny.style.display', 'block', '', 677);
    removeClass(_dynoReadAsArg('bunny', bunny, "removeClass", 0, 678), "dead");
    removeClass(_dynoReadAsArg('bunny', bunny, "removeClass", 0, 679), "poison");
    if (_dynoRead('bunny', bunny, 681)[_dynoReadProp("bunny", "id", 681)][_dynoFunc("bunny.id", "match", 681)](/bomb/)) 
    {
      bunny.style.marginRight = _dynoWrite('bunny.style.marginRight', '0px', '', 683);
      bunny.style.marginLeft = _dynoWrite('bunny.style.marginLeft', '0px', '', 684);
      bunny.style.marginTop = _dynoWrite('bunny.style.marginTop', '0px', '', 685);
      if (_dynoRead('bunny', bunny, 687)[_dynoReadProp("bunny", "id", 687)] == "bomb1") 
      {
        document.getElementById("bunny10").target = false;
      } else {
        document.getElementById("bunny11").target = false;
      }
      removeClass(_dynoReadAsArg('bunny', bunny, "removeClass", 0, 696), "exploded");
    } else {
      bunny.target = _dynoWrite('bunny.target', false, '', 700);
      bunny.direction = _dynoWrite('bunny.direction', 'up', '', 701);
    }
  } else {
    _dynoRead('bunny', bunny, 706)[_dynoReadProp("bunny", "blinkCounter", 706)]++;
    setTimeout(function() {
  blink(_dynoReadAsArg('bunny', bunny, "blink", 0, 710));
}, 500);
  }
  return true;
}
function getPosition(theElement) {
  _dynoWriteArg('theElement', theElement, 'getPosition', 0, 720);
  var positionX = _dynoWrite('positionX', 0, '', 722);
  var positionY = _dynoWrite('positionY', 0, '', 723);
  while (_dynoRead('theElement', theElement, 725) != null) 
    {
      positionX += _dynoWriteAug('positionX', _dynoRead('theElement', theElement, 727)[_dynoReadProp("theElement", "offsetLeft", 727)], 'theElement.offsetLeft', 727);
      positionY += _dynoWriteAug('positionY', _dynoRead('theElement', theElement, 728)[_dynoReadProp("theElement", "offsetTop", 728)], 'theElement.offsetTop', 728);
      theElement = _dynoWrite('theElement', _dynoRead('theElement', theElement, 729)[_dynoReadProp("theElement", "offsetParent", 729)], 'theElement.offsetParent', 729);
    }
  return [_dynoRead('positionX', positionX, 732), _dynoRead('positionY', positionY, 732)];
}

