<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../taglib.jsp" %>
<div>
	 <ul id="goodsCategory" class="easyui-tree">
    </ul>
</div>
<div id="goodsCategoryMenu" class="easyui-menu" style="width:120px;" data-options="onClick:menuHandler">
    <div data-options="iconCls:'icon-add',name:'add'">添加</div>
    <div data-options="iconCls:'icon-remove',name:'rename'">重命名</div>
    <div class="menu-sep"></div>
    <div data-options="iconCls:'icon-remove',name:'delete'">删除</div>
</div>
<script type="text/javascript">
$(function(){
	$("#goodsCategory").tree({
		url : 'goods/cat/list',
		animate: true,
		method : "GET",
		onContextMenu: function(e,node){
            e.preventDefault();
            $(this).tree('select',node.target);
            $('#goodsCategoryMenu').menu('show',{
                left: e.pageX,
                top: e.pageY
            });
        },
        onAfterEdit : function(node){
        	var _tree = $(this);
        	if(node.id == 0){
        		// 新增节点
        		$.post("/goods/cat/saveOrUpdate",{parentId:node.parentId,name:node.text},function(data){
        			if(data.status == 200){
        				$.messager.alert('提示','创建成功');
        				_tree.tree("update",{
            				target : node.target,
            				id : data.data.id
            			});
        			}else{
        				$.messager.alert('提示','创建'+node.text+' 分类失败!');
        			}
        		});
        	}else{
        		$.post("/goods/cat/saveOrUpdate",{id:node.id,name:node.text},function(data){
        			if(data.status == 200){
        				$.messager.alert('提示','修改成功');
        			}
        		});
        	}
        }
	});
});
function menuHandler(item){
	var tree = $("#goodsCategory");
	var node = tree.tree("getSelected");
	if(item.name === "add"){
		tree.tree('append', {
            parent: (node?node.target:null),
            data: [{
                text: '新建分类',
                id : 0,
                parentId : node.id
            }]
        }); 
		var _node = tree.tree('find',0);
		tree.tree("select",_node.target).tree('beginEdit',_node.target);
	}else if(item.name === "rename"){
		tree.tree('beginEdit',node.target);
	}else if(item.name === "delete"){
		$.messager.confirm('确认','确定删除名为 '+node.text+' 的分类吗？',function(r){
			if(r){
				$.post("/goods/cat/delete/",{id:node.id},function(data){
					if(data.status==-1){
						$.messager.alert('提示',data.msg);
					}else{
						tree.tree("remove",node.target);
					}
				});	
			}
		});
	}
}
</script>