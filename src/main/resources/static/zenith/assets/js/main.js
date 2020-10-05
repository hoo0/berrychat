(function($) {
    'use strict';
    
    var deviceList = [];
    var items = {};
    var code = {
        type: {
            ac    : '에어컨',
            light : '조명',
            gas   : '가스',
            temp  : '난방',
            vent  : '환풍기',
        },
        icon: {
            light : 'fe-sun',
            ac    : 'fe-wind',
            temp  : 'fe-thermometer',
        }
    };
    var deviceItem_template = '';
    var modal_backup = '';
    var houseNo = '';
    
    function getDeviceName( type, td ) {
        return td.length >= 2 ? code.type[type] + td.substring( td.length - 2 ) : td
    }
    
    function getDeviceList() {
        $.get(
            '/zenith/api/list',
            afterGetDeviceList
        );
    }
    
    function setDevice( type, code, action ) {
        // $.get(
        //     '/zenith/api/control/' + type + '/' + code + '/' + action,
        //     afterSetDevice
        // );
        
        getDeviceList();
    }
    
    function afterSetDevice( data, status ) {
        /* 실패이면 상태체크하고 checkbox 보정 */
        console.log( 'afterSetDevice: data', data );
        if (checkSession(data) == 'FAIL') return;
        getDeviceList();
    }
    
    function setDevice2( data ) {
        // $.post(
        //     '/zenith/api/control',
        //     data,
        //     afterSetDevice2
        // );
        
        $.ajax({
          url: '/zenith/api/control',
          type: "POST",
          data: JSON.stringify(data),
          contentType: "application/json; charset=utf-8",
          // dataType: "json",
          success: afterSetDevice2
        });
        
    }
    
    function afterSetDevice2( data, status ) {
        /* 실패이면 상태체크하고 checkbox 보정 */
        console.log( 'afterSetDevice2: data', data );
        if (checkSession(data) == 'FAIL') return;
        getDeviceList();
        
        //데이터 로드
        displayModal( data.type, data.td );
    }
    
    function afterGetDeviceList( data, status ) {
        console.log( 'afterGetDeviceList: data=', data );

        if (checkSession(data) == 'FAIL') return;
        
        deviceList = data.data;

        deviceList.sort( (a, b) => a.td < b.td ? -1 : a.td > b.td ? 1 : 0 );
        //console.log( 'deviceList=', deviceList );
        
        initData();
        
        deviceList.forEach( (item) => {
            //console.log( 'items[' + item.type + '] = ', items[item.type] );
            
            if ( items[item.type] === undefined ) items[item.type] = [];
            
            item.icon = code.icon[item.type]||'fe-help-circle';
            item.name2 = getDeviceName( item.type, item.td );
            
            items[item.type].push( item );
        });
        
        console.log( 'items = ', items );
        
        for ( var type in items ) {
            setItemsHtml( type );
        }
        
        setHouseNo( data.uid );
    }
    
    function initData() {
        items = {};
    }
    
    function setHouseNo( houseNo ) {
        // 0010203806
        houseNo = houseNo.length >= 10 ? Number( houseNo.substring(0,5) ) + '동 ' + Number( houseNo.substring(5,10) ) + '호' : houseNo;
        
	    $.template( 'houseNoTemplate', '${houseNo}' );
        $( '#houseNo' ).empty();
	    $.tmpl( 'houseNoTemplate', {houseNo: houseNo} ).appendTo( '#houseNo' );
    }
    
    function setItemsHtml( type ) {

        var itemList = items[type];
        var target = '#' + type + '-list';

        if ( $( target ).length <= 0 ) return;
        
        //console.log( 'itemList=', itemList );
        // $( '#deviceItem-template' ).tmpl( {list: itemList} ).appendTo( target );
        $( target ).empty();
        $.tmpl( 'deviceItem-template', {list: itemList} ).appendTo( target );
        
        /* click event */
        $( 'input:checkbox[id*=' + type + '-switch-]' ).click(function(e) {
            console.log('checkbox-click')
            e.stopPropagation(); // 부모로 전파를 차단

            var chk = $(this).is( ':checked' );
            var td = $(this).data( 'td' );
            console.log( $(this).attr( 'id' ) + '/' + td + ' = ' + chk );
            
            var action = '';
            if ( chk == true ) action = 'on';
            else action = 'off';
            
            $(this).prop( 'disabled', true );
            setTimeout( checkboxEnable( $(this) ), 3000 );
            
            setDevice( type, td, action );
        });
        
        $( 'a[id*=item-' + type + ']' ).click(function(e) {
            console.log('a-click e.target.tagName='+ e.target.tagName);
            //e.preventDefault(); // a-tag의 href이동을 차단
            
            if (e.target.tagName == 'LABEL') {
                console.log('return');
                return;
            }

            var data_type = $(this).data( 'type' );
            var data_td = $(this).data( 'td' );
            //console.log('click: type['+type+']');
            console.log('click: type['+data_type+'] td['+data_td+']');
            
            if ( data_type == 'ac') {
                //모달내용 초기화
                $( '.modal-content' ).html( modal_backup );
                //모달 띄우기, spinning
                $('#control-detail').modal('show');
                //데이터 로드
                displayModal( data_type, data_td );
            }
        });
        
    }
    
    function displayModal( type, td ) {
        $.get(
            '/zenith/api/status/'+type+'/'+td,
            displayModal2
        );
    }
    function displayModal2( data, status ) {
        console.log('displayModal2: status=', status);
        console.log('displayModal2: data=', data);
        
        if (checkSession(data) == 'FAIL') return;
        
        var o = data.data;
        o.title = getDeviceName( data.data.type, data.data.td );
        console.log('o=', o);
        
        //화면 변경
        $( '.modal-content' ).empty();
        $.tmpl( 'modal-template', o ).appendTo( '.modal-content' );
        
        $( 'input[type=number]' ).inputSpinner();
        
        $( '#modal-apply' ).click(function(e) {
            var value = $( '#temperature' ).val();
            var type = $(this).data( 'type' );
            var td = $(this).data( 'td' );
            
            console.log('type='+type+' td='+td+' value='+value);
            
            setDevice2({
                type: type,
                code: td,
                action: 'change',
                value: value
            });
        });
    }
    
    function checkSession(data) {
        if (data.messageCode == 'session-expired')
            document.location.href = '/zenith/login';
        
        return data.result ? data.result : 'FAIL';
    }
    
    function checkboxEnable( o ) { /* closure */
        return function() {
            o.prop( 'disabled', false );                    
            console.log( o.attr( 'id' ) + ' enabled' );
        };
    }
    
    function loadTemplates( urls ) {
        urls.forEach( (item) => {
            $.get('templates/'+item+'.html', function (data) {
               $.template( item+'-template', $(data).filter('#'+item+'-template').html() );
            });            
        });
    }

    //모달 백업
    modal_backup = $( '.modal-content' ).html();
    console.log('모달백업');
    
    $(document).ready(function() {
        console.log('ready');
        loadTemplates([
            'deviceItem',
            'modal',
        ]);
        
        getDeviceList();
        
        window.addEventListener('focus', function() {
            //getDeviceList();
            console.log('사용자가 웹페이지에 돌아왔습니다.');
        }, false);
    });

})(jQuery);
