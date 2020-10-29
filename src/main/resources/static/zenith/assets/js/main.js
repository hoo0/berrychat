(function($) {
    'use strict';
    
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
    var deviceList = [];
    var items = {};
    var detailData = {};
    var selectData = {};
    var deviceListTimestamp = 0;
    var deviceItem_template = '';
    var modal_backup = '';
    var houseNo1 = '';
    var houseNo2 = '';
    
    function getDeviceName( type, td ) {
        return td.length >= 2 ? code.type[type] + td.substring( td.length - 2 ) : td
    }
    
    function setDevice( data ) { /* for list */
        data = convertToString( data );
        
        $.ajax({
          url: '/zenith/api/control',
          type: "POST",
          data: JSON.stringify( data ),
          contentType: "application/json; charset=utf-8",
          // dataType: "json",
          success: afterSetDevice
        });
    }
    function afterSetDevice( data, status ) {
        console.log( 'afterSetDevice: data', data );
        if (checkSession(data) === 'LOGOUT') return;

        var delay = data.data.type === 'temp' ? 4000 : 1000;
        setTimeout(getDeviceList, delay);
    }
    
    function setDevice2( data ) { /* for modal */
        data = convertToString( data );
        
        $.ajax({
          url: '/zenith/api/control',
          type: "POST",
          data: JSON.stringify( data ),
          contentType: "application/json; charset=utf-8",
          success: afterSetDevice2
        });
    }
    function afterSetDevice2( data, status ) {
        console.log( 'afterSetDevice2: data', data );
        if (checkSession(data) === 'LOGOUT') return;
        
        var delay = data.data.type === 'temp' ? 4000 : 1000;
        setTimeout(function() {
            getDeviceList();
            showModal();
        }, delay);
    }

    function getDeviceList() {
        $.get(
            '/zenith/api/list',
            afterGetDeviceList
        );
    }
    function afterGetDeviceList( data, status ) {
        console.log( 'afterGetDeviceList: data=', data );
        if (checkSession(data) === 'LOGOUT' || data.result !== 'OK') return;
        
        deviceList = data.data;
        deviceList.sort( (a, b) => a.td < b.td ? -1 : a.td > b.td ? 1 : 0 );
        
        initData();
        
        deviceList.forEach( (item) => {
            if ( items[item.type] === undefined ) items[item.type] = [];
            
            item.icon = code.icon[item.type]||'fe-help-circle';
            item.name2 = getDeviceName( item.type, item.td );
            
            items[item.type].push( item );
        });
        // console.log( 'items = ', items );

        Object.keys(items).forEach( (type) => {
            setItemsHtml( type );
        });
        
        setHouseNo( data.uid );
    }
    
    function initData() {
        items = {};
    }
    
    function setHouseNo( houseNo ) {
        // 00XXX0YYYY => xxx동 yyy호
        if (houseNo.length >= 10) {
            houseNo1 = Number( houseNo.substring(0,5) );
            houseNo2 = Number( houseNo.substring(5,10) );
            houseNo = houseNo1 + '동 ' + houseNo2 + '호';
        }
        
	    $.template( 'houseNoTemplate', '${houseNo}' );
        //$( '#houseNo' ).empty();
	    //$.tmpl( 'houseNoTemplate', {houseNo: houseNo} ).appendTo( '#houseNo' );
        $( '#houseNo' ).html( $.tmpl( 'houseNoTemplate', {houseNo: houseNo} ) );
    }
    
    function setItemsHtml( type ) {
        var itemList = items[type];
        var target = '#' + type + '-list';

        if ( $( target ).length <= 0 ) return;
        
        $( target ).html( $.tmpl( 'deviceItem-template', {list: itemList} ) );
        
        /* power switch event */
        $( 'input:checkbox[id*=' + type + '-switch-]' ).click( (e) => {
            console.log('checkbox-click: type=' + type);
            e.stopPropagation(); // 부모로 전파를 차단
            var el = $(e.currentTarget);

            el.prop( 'disabled', true );
            setTimeout( checkboxEnable( el ), 3000 );

            var chk = el.is( ':checked' );
            var td = el.data( 'td' );
            console.log( el.attr( 'id' ) + '/' + td + ' = ' + chk );
            
            changeMode({
                type: type,
                td: td,
                mode: chk ? 'ON' : 'OFF',
                action: 0
            });
        });
        
        /* modal popup event */
        $( 'a[id*=item-' + type + ']' ).click( (e) => {
            //e.preventDefault(); // 이벤트의 전파를 막지않고 그 이벤트를 취소
            // console.log('a-click e.target.tagName='+ e.target.tagName);
            var el = $(e.currentTarget);
            if (e.target.tagName == 'LABEL') return;

            var data_type = el.data( 'type' );
            var data_td   = el.data( 'td'   );
            console.log('type=' + data_type +' td=' + data_td);
            
            if ( data_type === 'ac'   ||
                 data_type === 'temp' ) {
                //선택
                selectData.type = data_type;
                selectData.td   = data_td  ;
                
                //모달내용 초기화
                $( '.modal-content' ).html( modal_backup );
                //모달 띄우기, spinning
                $( '#control-detail' ).modal( 'show' );
                //데이터 로드
                showModal();
            }
        });
    }
    
    function checkboxEnable( o ) { /* closure */
        return function() {
            o.prop( 'disabled', false );                    
            console.log( o.attr( 'id' ) + ' enabled' );
        };
    }
    
    function setParams( o ) {
        //type=light&code=lt01&cmd=set&power=100
        //type=ac&code=ac01&cmd=set&cmd2=power&power=1  &td=ac01&state=on  &status=&rm=0&ht=25&ct=27&ac=0&cc=0&sc=0&as=16&ef=0&rnt=0&rft=0&rt=0
        //type=temp&code=dt02&cmd=set&power=1  &rm=0&st=0&to=0&tf=0&ct=0&vs=0&rs=0&es=0
        var power = o.mode === 'ON' || o.mode === 'OUT' ? (o.type === 'light' ? '100' : '1') : '0';
        var params = {
            type: o.type,
            code: o.td,
            td: o.td,
            cmd: 'set',
            power: power
        };

        if (detailData.td !== undefined && detailData.td === o.td) {
            if (detailData.type === 'ac') {
                // params.ht = detailData.ht;
                params.as = detailData.as;
                params.ef = detailData.ef;
            }
        }

        if (o.type === 'ac') {
            $.extend(params, {
                state: o.mode.toLowerCase(),
                cmd2: 'power'
            });
        } else if (o.type === 'temp') {
            $.extend(params, {
                state: o.mode.toLowerCase(),
                rm: o.mode === 'OUT' ? '4' : (o.mode === 'ON' ? '1' : '0'),
                st: '25',
                to: '254',
                tf: '254',
                vs: '0',
                rs: '0',
                es: '0'
            });
        }
        
        return params;
    }

    function changeMode( o ) {
        var params = setParams( o );
        console.log('params=', params);
        
        if (o.action === 0) setDevice( params ); /* for list */
        else setDevice2( params ); /* for modal */
    }
    
    function isEmpty( o, keyNames ) {
        keyNames.forEach( (key) => {
            if ( o[key] == undefined || o[key] == null || o[key] == '') return true;
            // console.log('isEmpty: '+key+'='+o[key]);
        });
        return false;
    }

    function showModal() {
        if (isEmpty(selectData, ['type','td'])) return;

        $.get(
            '/zenith/api/status/'+selectData.type+'/'+selectData.td,
            afterShowModal
        );
    }
    function afterShowModal( data, status ) {
        // console.log('afterShowModal: status=', status);
        console.log('afterShowModal: data=', data);

        if (isEmpty(data, ['data']) || isEmpty(data.data, ['type', 'td'])) return;
        if (checkSession(data) === 'FAIL') return; /* 에러면 메시지 보여주고 닫는게 나을듯 */
        
        detailData = data.data;
        
        var o = data.data;
        o.title = getDeviceName( data.data.type, data.data.td );
        if (o.type === 'ac') o.tt = o.ht;
        else if (o.type === 'temp') o.tt = o.st;
        console.log('o=', o);
        
        //spinning -> modal content
        $( '.modal-content' ).html( $.tmpl( 'modal-template', o ) );
        
        $( '#temperature' ).inputSpinner({buttonsOnly: true});
        
        var mode = '';
        if (o.type === 'temp') {
            if (o.power === 255) {
                if (o.rm === 1) mode = 'ON';
                else mode = 'OUT';
            } else if (o.power === 0) mode = 'OFF';

        } else if (o.type === 'ac') {
            if (o.power === 1) mode = 'ON';
            else if (o.power === 0) mode = 'OFF';
        }
        
        if (mode === 'ON' || mode === 'OUT' || mode === 'OFF') $( '#tab'+mode ).tab( 'show' );

        if (mode === 'ON') {
            // 온도spinner 보이기
            $( '#modal-form' ).removeClass( 'd-none' );
            
            $( '#temperature' ).on("change", function (event) {
                if ( o.tt != $(this).val() ) $( '#modal-apply' ).removeClass( 'd-none' );
                else $( '#modal-apply' ).addClass( 'd-none' );
            })
        } else {
            $( '#modal-form' ).addClass( 'd-none' );
        }
        
        $('a[data-toggle="tab"][id^="tab"]').on('shown.bs.tab', (e) => {
            // $( '#modal-form' ).addClass( 'd-none' );
            $( '.modal-content' ).html( modal_backup );
            
            var el = $(e.currentTarget);
            var p = {
                type: o.type,
                td: o.td,
                mode: el.data( 'mode' ),
                action: 1
            };
            console.log('p=', p);
            changeMode( p );
        })
        
        $( '#modal-apply' ).click( (e) => {
            $( '#modal-apply' ).addClass( 'd-none' );
            
            console.log('selectData=', selectData);
            var value = $( '#temperature' ).val();
            if (selectData.td !== o.td) return;

            var params = setParams( { type: selectData.type, td: selectData.td, mode: 'ON' });
            
            if (params.type === 'ac') {
                params.cmd2 = 'condition';
                params.ht = value;
                params.rm = 128;
            } else if (params.type === 'temp') params.st = value;

            console.log('params=', params);
            setDevice2( params ); /* for modal */
        });
    }
    
    function convertToString( oldData ) {
        var newData = {};
        for (var key in oldData) newData[key] = oldData[key].toString();
        return newData;
    }
    
    function checkSession( data ) {
        if (data.messageCode === 'session-expired') {
            document.location.href = '/zenith/login';
            return 'LOGOUT';
        }
        return data.result ? data.result : 'FAIL';
    }
    
    function loadTemplates( urls ) {
        urls.forEach( (item) => {
            $.get('templates/'+item+'.html', (data) => {
               $.template( item+'-template', $(data).filter('#'+item+'-template').html() );
            });            
        });
    }

    //모달 백업
    modal_backup = $( '.modal-content' ).html();
    // console.log('모달백업');
    
    $(document).ready(function() {
        // console.log('ready');
        loadTemplates([
            'deviceItem',
            'modal',
        ]);
        
        getDeviceList();
        
        window.addEventListener('focus', () => {
            var timegap = (new Date()).getTime() - deviceListTimestamp;
            console.log('사용자가 웹페이지에 돌아왔습니다.[' + timegap + ']');
            // (timegap > 10 * 1000) && getDeviceList();
        }, false);
        window.addEventListener('blur', () => {
            console.log('사용자가 웹페이지를 떠났습니다.');
            deviceListTimestamp = (new Date()).getTime();
        }, false);
        
    });
    
})(jQuery);
