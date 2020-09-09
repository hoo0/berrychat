(function($) {

    "use strict";
    
    var deviceList = [];
    var items = {};
    var deviceLightList = [];
    var deviceAirconList = [];
    
    function getDeviceList() {
        $.get(
            '/zenith/api/list2',
            processDeviceList
        );
    }
    
    function processDeviceList(data, status) {
        console.log('data=', data);
        deviceList = data.data;

        function compare(a, b) {
            return a.td < b.td ? -1 : a.td > b.td ? 1 : 0;
        }

        deviceList.sort(compare);
        console.log('deviceList=', deviceList);
        
        deviceList.forEach((item) => {
            console.log('items['+item.type+'] = ', items[item.type]);
            
            if (items[item.type] === undefined) items[item.type] = [];
            
            item.icon = 'fe-help-circle';
            if (item.type == 'light') item.icon = 'fe-sun';
            else if (item.type == 'ac') item.icon = 'fe-wind';
            else if (item.type == 'temp') item.icon = 'fe-thermometer';
            
            items[item.type].push(item);
        });
        
        console.log('items = ', items);
        
        for (var key in items) {
            makeItemsHtml(key);
        }

    }
    
    function makeItemsHtml(key) {
        
        /* insert light */
        var itemList = items[key];
        var target = '#'+key+'-list';

        if ($(target).length <= 0) return;
        
        console.log('itemList=', itemList);
        $("#deviceItem-template").tmpl({list: itemList}).appendTo(target);
        
        /* light event */
        $('input:checkbox[id*='+key+'-switch-]').click(function(){
            var chk = $(this).is(":checked");
            var td = $(this).data('td');
            console.log($(this).attr('id')+ '/'+td+' = ' + chk);
            
            $(this).prop("disabled", true);
            setTimeout(checkboxEnable($(this)), 3000);
            
        });
    }
    
    function checkboxEnable(o) {
        return function() {
            console.log(o.attr('id')+' enabled');
            o.prop("disabled", false);                    
        };
    }
    
    

    $(document).ready(function() {
        $('#templates').load("templates/deviceItem.html");
        
        getDeviceList();
    
    });

})(jQuery);