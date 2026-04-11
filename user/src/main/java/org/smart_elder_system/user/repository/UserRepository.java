package org.smart_elder_system.user.repository;

import org.smart_elder_system.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户JPA仓库接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deleteFlag = 0")
    Optional<User> findByUsername(@Param("username") String username);
    
    /**
     * 根据手机号查找用户
     *
     * @param phone 手机号
     * @return 用户
     */
    @Query("SELECT u FROM User u WHERE u.phone = :phone AND u.deleteFlag = 0")
    Optional<User> findByPhone(@Param("phone") String phone);
    
    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleteFlag = 0")
    Optional<User> findByEmail(@Param("email") String email);
    
    /**
     * 根据身份证号查找用户
     *
     * @param idCard 身份证号
     * @return 用户
     */
    @Query("SELECT u FROM User u WHERE u.idCard = :idCard AND u.deleteFlag = 0")
    Optional<User> findByIdCard(@Param("idCard") String idCard);
    
    /**
     * 根据用户名和状态查找用户
     *
     * @param username 用户名
     * @param status 状态
     * @return 用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.status = :status AND u.deleteFlag = 0")
    Optional<User> findByUsernameAndStatus(@Param("username") String username, @Param("status") Integer status);
    
    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deleteFlag = 0")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.phone = :phone AND u.deleteFlag = 0")
    boolean existsByPhone(@Param("phone") String phone);
    
    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deleteFlag = 0")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * 检查身份证号是否存在
     *
     * @param idCard 身份证号
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.idCard = :idCard AND u.deleteFlag = 0")
    boolean existsByIdCard(@Param("idCard") String idCard);
    
    /**
     * 检查用户名是否存在（带删除标记）
     *
     * @param username 用户名
     * @param deleteFlag 删除标记
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deleteFlag = :deleteFlag")
    boolean existsByUsernameAndDeleteFlag(@Param("username") String username, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 检查手机号是否存在（带删除标记）
     *
     * @param phone 手机号
     * @param deleteFlag 删除标记
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.phone = :phone AND u.deleteFlag = :deleteFlag")
    boolean existsByPhoneAndDeleteFlag(@Param("phone") String phone, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 检查邮箱是否存在（带删除标记）
     *
     * @param email 邮箱
     * @param deleteFlag 删除标记
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deleteFlag = :deleteFlag")
    boolean existsByEmailAndDeleteFlag(@Param("email") String email, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 检查身份证号是否存在（带删除标记）
     *
     * @param idCard 身份证号
     * @param deleteFlag 删除标记
     * @return 是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.idCard = :idCard AND u.deleteFlag = :deleteFlag")
    boolean existsByIdCardAndDeleteFlag(@Param("idCard") String idCard, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 根据用户名、状态和删除标记查询用户
     *
     * @param username 用户名
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.status = :status AND u.deleteFlag = :deleteFlag")
    User findByUsernameAndStatusAndDeleteFlag(@Param("username") String username, @Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 根据手机号、状态和删除标记查询用户
     *
     * @param phone 手机号
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 用户
     */
    @Query("SELECT u FROM User u WHERE u.phone = :phone AND u.status = :status AND u.deleteFlag = :deleteFlag")
    User findByPhoneAndStatusAndDeleteFlag(@Param("phone") String phone, @Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 根据邮箱、状态和删除标记查询用户
     *
     * @param email 邮箱
     * @param status 状态
     * @param deleteFlag 删除标记
     * @return 用户
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = :status AND u.deleteFlag = :deleteFlag")
    User findByEmailAndStatusAndDeleteFlag(@Param("email") String email, @Param("status") Integer status, @Param("deleteFlag") Integer deleteFlag);
    
    /**
     * 根据关键词和删除标记分页查询用户
     *
     * @param keyword 关键词
     * @param deleteFlag 删除标记
     * @param pageable 分页参数
     * @return 用户分页
     */
    @Query("SELECT u FROM User u WHERE (u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR u.phone LIKE %:keyword% OR u.email LIKE %:keyword%) AND u.deleteFlag = :deleteFlag ORDER BY u.createTime DESC")
    Page<User> findByKeywordAndDeleteFlag(@Param("keyword") String keyword, @Param("deleteFlag") Integer deleteFlag, Pageable pageable);
    
    /**
     * 根据删除标记按创建时间倒序分页查询用户
     *
     * @param deleteFlag 删除标记
     * @param pageable 分页参数
     * @return 用户分页
     */
    @Query("SELECT u FROM User u WHERE u.deleteFlag = :deleteFlag ORDER BY u.createTime DESC")
    Page<User> findByDeleteFlagOrderByCreateTimeDesc(@Param("deleteFlag") Integer deleteFlag, Pageable pageable);
    
    /**
     * 根据用户名模糊查询用户列表
     *
     * @param keyword 关键词
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR u.username LIKE %:keyword% OR " +
           "u.realName LIKE %:keyword% OR u.phone LIKE %:keyword% OR u.email LIKE %:keyword%) " +
           "AND u.deleteFlag = 0 ORDER BY u.createTime DESC")
    List<User> findByKeyword(@Param("keyword") String keyword);
    
    /**
     * 根据用户名查找用户及其角色
     *
     * @param username 用户名
     * @return 用户及其角色
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles " +
           "WHERE u.username = :username AND u.deleteFlag = 0")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
    
    /**
     * 统计用户数量
     *
     * @param keyword 关键词
     * @param status 状态
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR u.username LIKE %:keyword% OR " +
           "u.realName LIKE %:keyword% OR u.phone LIKE %:keyword% OR u.email LIKE %:keyword%) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND u.deleteFlag = 0")
    long countByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Integer status);

    /**
     * 根据身份证号和删除标记查找用户
     */
    @Query("SELECT u FROM User u WHERE u.idCard = :idCard AND u.deleteFlag = :deleteFlag")
    Optional<User> findByIdCardAndDeleteFlag(@Param("idCard") String idCard, @Param("deleteFlag") Integer deleteFlag);
}