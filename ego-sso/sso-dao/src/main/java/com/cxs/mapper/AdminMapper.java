package com.cxs.mapper;

import com.cxs.domain.Admin;
import com.cxs.domain.AdminExample;
import com.cxs.domain.AdminWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author cxs
 */
public interface AdminMapper {

    long countByExample(AdminExample example);

    int deleteByExample(AdminExample example);

    int deleteByPrimaryKey(Short id);

    int insert(AdminWithBLOBs record);

    int insertSelective(AdminWithBLOBs record);

    List<AdminWithBLOBs> selectByExampleWithBLOBs(AdminExample example);

    List<Admin> selectByExample(AdminExample example);

    AdminWithBLOBs selectByPrimaryKey(Short id);

    int updateByExampleSelective(@Param("record") AdminWithBLOBs record, @Param("example") AdminExample example);

    int updateByExampleWithBLOBs(@Param("record") AdminWithBLOBs record, @Param("example") AdminExample example);

    int updateByExample(@Param("record") Admin record, @Param("example") AdminExample example);

    int updateByPrimaryKeySelective(AdminWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(AdminWithBLOBs record);

    int updateByPrimaryKey(Admin record);
}