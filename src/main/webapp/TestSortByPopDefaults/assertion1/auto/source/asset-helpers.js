var renderAssets, mouseStop, renderAssetsScroll;

(function () {
    renderAssets = function (data) {
        var el = $('.store-left');
        caramel.css($('head'), data.body['sort-assets'].resources.css, 'sort-assets');
        caramel.code($('head'), data.body['assets'].resources.code);
        caramel.partials(data._.partials, function () {
            var assets = Handlebars.partials['assets'](data.body.assets.context),
                sort = Handlebars.partials['sort-assets'](data.body['sort-assets'].context);
            theme.loaded(el, sort);
            el.append(assets);
            caramel.js($('body'), data.body['assets'].resources.js, 'assets', function () {
                mouseStop();
            });
            caramel.js($('body'), data.body['sort-assets'].resources.js, 'sort-assets', function () {
                updateSortUI();
            });
            $(document).scrollTop(0);

            infiniteScroll = data.body.assets.context.assets.length >= 12;
        });
    };

    renderAssetsScroll = function(data){
    	var temp = '{{#slice assets size="4"}}&lt;div class="row-fluid">';
        	temp += '{{#each .}}';
			temp += '&lt;div class="span3 asset" data-id="{{id}}" data-path="{{path}}" data-type="{{type}}">';
			temp += '	{{#attributes}}';
			temp += '	&lt;a href="{{url "/assets"}}/{{../type}}/{{../id}}">';
			temp += '	&lt;div class="asset-icon">';	
			temp += '		{{#if ../indashboard}}';	
			temp += '				&lt;i class="icon-bookmark store-bookmark-icon">&lt;/i>';	
			temp += '		{{/if}}';		
			temp += '	&lt;img src="{{#if images_thumbnail}}{{images_thumbnail}}{{/if}}">';
			temp += '	&lt;/div> &lt;/a>';
			temp += '	&lt;div class="asset-details">';
			temp += '		&lt;div class="asset-name">';
			temp += '			&lt;a href="{{url "/assets"}}/{{../type}}/{{../id}}"> &lt;h4>{{overview_name}}&lt;/h4> &lt;/a>';
			temp += '		&lt;/div>';
			temp += '		&lt;div class="asset-rating">';
			temp += '			&lt;div class="asset-rating-{{../rating/average}}star">';
			temp += '			&lt;/div>';
			temp += '		&lt;/div>';
			temp += '		&lt;div class="asset-author-category">';
			temp += '			&lt;ul>';
			temp += '				&lt;li>';
			temp += '					&lt;h4>{{t "Version"}}&lt;/h4>';
			temp += '					&lt;a class="asset-version" href="#">{{overview_version}}&lt;/a>';
			temp += '				&lt;/li>';
			temp += '				&lt;li>';
			temp += '					&lt;h4>{{t "Category"}}&lt;/h4>';
			temp += '					&lt;a class="asset-category" href="#">{{cap ../type}}&lt;/a>';
			temp += '				&lt;/li>';
			temp += '				&lt;li>';
			temp += '					&lt;h4>{{t "Author"}}&lt;/h4>';
			temp += '					&lt;a class="asset-author" href="#">{{overview_provider}}&lt;/a>';					
			temp += '				&lt;/li>';
			temp += '			&lt;/ul>';
			temp += '			{{#if ../indashboard}}';
			temp += '			&lt;a href="#" class="btn disabled btn-added">{{t "Bookmarked"}}&lt;/a>';
			temp += '			{{else}}';
			temp += '				{{# if ../../../../sso}}';			
			temp += '				&lt;a href="{{url "/login"}}" class="btn btn-primary asset-add-btn">{{t "Bookmark"}}&lt;/a>';
			temp += '				{{else}}';							
			temp += '					&lt;a href="#" class="btn btn-primary asset-add-btn">{{t "Bookmark"}}&lt;/a>';
			temp += '				{{/if}}';
			temp += '				{{# if ../../../../user.username}}';		
			temp += '				&lt;a href="#" class="btn btn-primary asset-add-btn">{{t "Bookmark"}}&lt;/a>';
			temp += '				{{/if}}';
			temp += '			{{/if}}';
			temp += '		&lt;/div>';
			temp += '	&lt;/div>';
			temp += '	{{/attributes}}';
			temp += '&lt;/div>';
			temp += '{{/each}}';
			temp += '&lt;/div>{{/slice}}';
			
      var assetsTemp = Handlebars.compile(temp);
 	  var render = assetsTemp(data.body.assets.context);
      $('#assets-container').append(render);
      
       caramel.js($('body'), data.body['assets'].resources.js, 'assets', function () {
                mouseStop();
            });
    	
    };

    mouseStop = function () {
    	var windowWidth = $(window).width();
    	var offsetTop = windowWidth &lt; 980 ? 167 : 200;
        var id;
        $('.asset').mousestop(function () {
            var that = $(this);
            id = setTimeout(function () {
		that.find('.store-bookmark-icon').animate({
		    top : -200
		}, 200);
                that.find('.asset-details').animate({
                    top: 0
                }, 200);
            }, 300);
        }).mouseleave(function () {
                clearTimeout(id);
		$(this).find('.store-bookmark-icon').animate({top: -4}, 200);
                $(this).find('.asset-details').animate({top: offsetTop}, 200);
            });
    };
}());

