/* jshint esversion: 6 */
function ewacConfDesign(SYS_FRAME_UNID, rst) {
    var ewa = EWA.F.FOS[SYS_FRAME_UNID];

    function initParametes(rst) {
        ewa._conf_para = $J2MAP1(rst[0], 'CONF_TAG');
        ewa._conf_dict = $J2MAP1(rst[1], 'CONF_TAG,PARA_NAME');
        let arrconfs = rst[2];
        let map = {};
        for (var n in arrconfs) {
            let conf = arrconfs[n];
            conf.children = [];
            map[conf.CONF_TAG] = conf;
        }
        for (let n in arrconfs) {
            let conf = arrconfs[n];
            if (!conf.CONF_PARENT_TAG) {
                continue;
            }
            map[conf.CONF_PARENT_TAG].children.push(conf);
        }
        ewa._confs = map;

        let navigator = [];
        getObj('.ewa-lf-data-row').each(function () {
            let tag = $(this).attr('ewa_key');
            let tr = $('<tr class="pbox-parent" id="new_' + tag + '"><td colspan=3></td></tr>').insertAfter($(this));
            let conf = ewa._confs[tag];
            let box = createBox(conf);
            tr.find('td:last').append(box);

            let item = "<div><a tag='" + tag + "'>" + tag + " " + conf.CONF_MEMO + "</a></div>";
            navigator.push(item);
        });
        $('#conf_navigator').append(navigator.join(''));
        $('#conf_navigator a').on('click', function () {
            let tag = $(this).attr('tag');
            let target = $('tr[ewa_key="' + tag + '"]').position();
            $('#confs').scrollTop(target.top, 300);
        });

        // Create multiple parameters from the dict's name
        initAddParameters();
        initSelectValues();

        $('a#dspall').on('click', function () {
            $('tr.pbox-parent').show();
        });
        $('a#hidall').on('click', function () {
            $('tr.pbox-parent').hide();
        });

        $('a#export').on('click', function () {
            exportConf();
        });
        $('a#import').on('click', function () {
            importEwaConfLocal();
        });

        getObj('table[name=security] input[name=key],input[name=code]').parent().prev().append("<a class='randomcode'> 生成随机码</a>");
        getObj('a.randomcode').on('click', function () {
            randomCode(32, this);
        });
    }

    function initSelectValues() {
        for (let n in ewa._conf_dict) {
            if (n.indexOf(',name') > 0) {
                continue;
            }
            let tags = n.split(',');
            let conf_tag = tags[0];
            let para_name = tags[1];

            let arr = ewa._conf_dict[n];

            let datalistId = 'ref_' + conf_tag + '_' + para_name;
            let ss = ['<datalist id="' + datalistId + '">'];
            arr.map(v => {
                let name = v.DICT_NAME;
                let text = v.DICT_MEMO;
                let opt = "<option value='" + name + "'>" + text + "</option>";
                ss.push(opt);
            });
            ss.push('</datalist>');
            $('body').append(ss.join(''));

            getObj('table[name="' + conf_tag + '"] input[name="' + para_name + '"]').attr('list', datalistId);

        }
    }

    function initAddParameters() {
        for (let n in ewa._conf_dict) {
            if (n.indexOf(',name') < 0) {
                continue;
            }

            let tags = n.split(',');
            let conf_tag = tags[0];
            let para_name = tags[1];

            let arr = ewa._conf_dict[n];
            // find the add button
            let btnAdd = $('.add.tag-' + conf_tag);

            // The table where the add button is located
            let parent = btnAdd.parentsUntil('.pbox').last().parent().parent();
            let i = 0;
            arr.map(v => {
                if (i > 0) {
                    btnAdd.trigger('click');
                }
                i++;
                let jq = 'input[name="' + para_name + '"]:last';
                // find the last input
                let ipt = parent.find(jq);
                ipt.val(v.DICT_NAME);
                // the TD.EWA_TD_R
                ipt.parent().next().text(v.DICT_MEMO).attr('changed', 'yes');
            });
        }
    }

    function getObj(exp) {
        return exp ? $('#EWA_LF_' + SYS_FRAME_UNID).find(exp) : $('#EWA_LF_' + SYS_FRAME_UNID);
    }


    /**
     * Create a parameters table
     */
    function createBox(conf) {
        let tb = $(createParasBox(conf));
        conf.children.map(chdConf => {
            let chdTb = createChd(conf, chdConf, tb);
            tb.find('td:last').append(chdTb);
        });
        return tb;
    }

    function createButtons(conf, chdConf) {
        let a;
        if (conf.CONF_PARENT_TAG && chdConf.CONF_SINGLE == 'Y') {
            a = '<a class="closesetting tag-' + chdConf.CONF_TAG + '">关闭设置</a>';
        } else {
            a = '<a class="add tag-' + chdConf.CONF_TAG + '">添加一组</a> | <a class="remove tag-' +
                chdConf.CONF_TAG + '">删除本组</a>';
        }
        return a;
    }

    function createChd(conf, chdConf, tb) {
        let chdTb = createBox(chdConf);
        let buttons = createButtons(conf, chdConf);
        let tr = $('<tr><td class="EWA_TD_L">' + chdConf.CONF_TAG +
            '</td><td style="text-align:center" class="EWA_TD_M">' +
            buttons + '</td><td class="EWA_TD_R">' + chdConf.CONF_MEMO +
            '</td></tr><tr><td style="padding: 0 15px" colspan=3></td></tr>');
        tb.append(tr);

        tr.find('a.closesetting').on('click', function () {
            let target = $(this).parent().parent().next();
            if ($(this).attr('closed')) {
                $(this).attr('closed', '').text('关闭设置');
                target.show();
            } else {
                $(this).attr('closed', 'yes').text('打开设置');
                target.hide();
            }
        });
        tr.find('a.add').on('click', function () {
            let target = $(this).parent().parent().next().find('>td>.pbox').last();
            let clone = target.clone(true);
            clone.removeAttr("tag-set");
            clone.find('input').val("");
            if (clone.find('.btn-remove').length === 0) {
                clone.find('td:eq(0)').append('<a class="fa fa-remove btn-remove"></a>');
                clone.find('.btn-remove').on('click', function () {
                    let target = $(this).parent().parent().parent().parent();
                    target.remove();
                });
            }
            target.parent().append(clone);
        });
        return chdTb;
    }

    function createParasBox(conf) {
        let paras = ewa._conf_para[conf.CONF_TAG];
        let ss = ["<table class='pbox' name='" + conf.CONF_TAG + "'>"];
        for (let n in paras) {
            let p = paras[n];
            let s = '<tr><td class="EWA_TD_L">' + p.PARA_NAME +
                '</td><td  class="EWA_TD_M"><input type=text name="' + p.PARA_NAME + '"></td><td  class="EWA_TD_R">' + p.PARA_MEMO + '</td></tr>';
            ss.push(s);
        }
        ss.push('</table>');

        return ss.join('');
    }

    function showExportDialog(xmlCode) {
        let xmlCodeTxt = $('#xmlCodeTxt');
        if (xmlCodeTxt.length == 0) {
            xmlCodeTxt = $('<textarea style="display:none" id=xmlCodeTxt></textarea>');
            $('body').append(xmlCodeTxt);
        }
        xmlCodeTxt.val(xmlCode);
        let url = (window.EWA.RV_STATIC_PATH || '/EmpScriptV2') + '/EWA_STYLE/editor/CodeMirror/index.html?__TYPE__=xml&__SID__=xmlCodeTxt';
        let height = document.documentElement.clientHeight - 88;
        let dia = $Dialog(url, 'ewa_conf.xml', 1000, height, true, function () {
            let w = dia.getContent().getElementsByTagName('iframe')[0].contentWindow;
            w.beautify_code();
            w.editor.setFontSize(14.3);

            var blob = new Blob([w.editor.getValue()], {
                type: "text/xml"
            });

            let btnSave = w.$('<div><a>Download</a></div>');
            btnSave.css({
                'position': 'fixed',
                'left': 0,
                'right': 0,
                'bottom': 0,
                'background': '#fff',
                'height': 30,
                'z-index': 123,
                'display': 'flex',
                'align-items': 'center',
                'justify-content': 'center'
            });
            btnSave.find('a').css({
                'line-height': '26px',
                'font-size': 14.1
            }).attr({
                'download': 'ewa_conf.xml',
                'href': URL.createObjectURL(blob)
            });
            w.$('body').append(btnSave);
            w.$('#code').css('bottom', 30);

            console.log(1);
        });
    }

    function exportConf() {
        xml = new EWA_XmlClass();
        xml.LoadXml('<?xml version="1.0" encoding="UTF-8" standalone="yes" ?><ewa_confs />');

        let root = xml.XmlDoc.children[0];

        getObj('.ewa-lf-data-row').each(function () {
            let chk = $(this).find('input[type=checkbox]:eq(0)').prop('checked');
            if (chk) {
                $(this).next().find('>td>.pbox').each(function () {
                    exportConfOne(this, root, xml);
                });
            }
        });

        let xmlCode = xml.GetXml();
        showExportDialog(xmlCode);
    }

    function exportConfOne(tb, parent, xml) {
        let obj = $(tb);
        let name = obj.attr('name');
        let conf = ewa._confs[name];
        let space = xml.XmlDoc.createTextNode("\r\n");
        parent.appendChild(space);
        let comment = xml.XmlDoc.createComment(" " + conf.CONF_MEMO.trim() + " ");
        parent.appendChild(comment);
        let chd = xml.NewChild(name, parent);
        let tb1 = obj[0];
        for (let i = 0; i < tb1.rows.length; i++) {
            let tr = tb1.rows[i];
            let isSub = false;
            if (i === tb1.rows.length - 1) {
                let subTbs = $(tr).find('>td>.pbox');
                if (subTbs.length > 0) {
                    isSub = true;
                    for (let m = 0; m < subTbs.length; m++) {
                        exportConfOne(subTbs[m], chd, xml);
                    }
                }
            }
            if (!isSub) {
                setParameter(tr, chd, comment);
            }
        }
    }

    function setParameter(tr, chd, comment) {
        let ipx = $(tr).find('input');
        if (ipx.length === 0) {
            return;
        }
        let name = ipx.attr('name');
        chd.setAttribute(name.trim(), ipx.val());
        if (name !== 'name') {
            return;
        }

        let tdr = $(tr).find('.EWA_TD_R');
        if (tdr.attr('changed')) {
            comment.textContent = tdr.text();
        }

    }

    function randomCode(len, anchor) {
        var s = [];
        var inc = 0;
        var inc1 = 0;
        while (inc < len) {
            inc1++;
            if (inc1 > 2000) {
                break;
            }
            var a = Math.round(Math.random() * 127);
            if (a >= 33 && a <= 126) {
                var c = String.fromCharCode(a);
                if (c == '+' || c == '=' || c == '&' || c == '>' || c == '<' || c == '"') {
                    continue;
                }
                s.push(c);
                inc++;
            }
        }
        if (anchor) {
            $(anchor).parent().next().find('input').val(s.join(''));
        }
        return s.join('');
    }

    function importEwaConfLocal() {
        if (window.location.href.indexOf('file') >= 0) {
            $Tip("不能在file模式执行");
            return;
        }
        var u = EWA.CP + '/EWA_DEFINE/cgi-bin/xml/?type=EWA_CONF';
        $J(u, function (rst) {
            if (rst.RST) {
                importEwaConfXml2(rst.XML);
            } else {
                alert(rst.ERR);
            }
        });
    }

    function importEwaConfXml2(xmlStr) {
        var xml = new EWA_XmlClass();
        xml.LoadXml(xmlStr);
        let tempTag = EWA_Utils.tempId('S');
        for (let conf_name in ewa._confs) {
            let nl = xml.XmlDoc.getElementsByTagName(conf_name);
            for (let i = 0; i < nl.length; i++) {
                let item = nl[i];
                if (item.tagName == 'remote_sync') {
                    continue;
                }
                setValueToParameters(item, tempTag);
            }
        }
        let inc = 0;
        let nl = xml.XmlDoc.getElementsByTagName("remote_syncs");
        for (let i = 0; i < nl.length; i++) {
            let parent = getObj('table[name="remote_syncs"]:eq(' + i + ')');

            let item = nl[i];
            let nla = item.getElementsByTagName("remote_sync");
            for (let ia = 0; ia < nla.length; ia++) {
                let itema = nla[ia];
                inc++;
                if (inc > 400) {
                    return;
                }
                console.log(itema);
                setValueToParameters(itema, tempTag, parent);
            }
        }
    }

    function setValueToParameters(item, tempTag, parent) {
        let tag = item.tagName;
        let obj = null;
        let pObj = parent || getObj();
        pObj.find('table[name="' + tag + '"]').each(function () {
            if (tempTag !== $(this).attr("tag-set")) {
                obj = $(this);
            }
        });
        if (obj === null) {
            pObj.find('.add.tag-' + tag).trigger('click');
            obj = pObj.find('table[name="' + tag + '"]').last();
        }
        obj.attr("tag-set", tempTag);

        for (var ia = 0; ia < item.attributes.length; ia++) {
            var a = item.attributes[ia];
            var id = a.nodeName;
            var v = a.nodeValue;
            obj.find('input[name="' + id + '"]').val(v);
        }
    }

    initParametes(rst);
}