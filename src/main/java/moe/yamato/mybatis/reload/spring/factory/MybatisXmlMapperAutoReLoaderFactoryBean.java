//package moe.yamato.mybatis.reload.spring.factory;
//
//import moe.yamato.mybatis.reload.xml.mapper.MybatisXmlMapperReloaderFactory;
//import org.apache.ibatis.session.SqlSession;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.FactoryBean;
//import org.springframework.beans.factory.InitializingBean;
//
///**
// * Package: com.lianjia.sh.mybatis.reload
// * Author: mac
// * Date: 2017/1/10
// */
//public class MybatisXmlMapperAutoReLoaderFactoryBean implements FactoryBean<MybatisXmlMapperReloaderFactory>, InitializingBean, DisposableBean {
//
//    // 是否启用热加载.
//    private boolean enableAutoReload = true;
//
//    private SqlSession sqlSession;
//
//    private MybatisXmlMapperReloaderFactory mybatisXmlMapperAutoReLoader;
//
//    @Override
//    public MybatisXmlMapperReloaderFactory getObject() throws Exception {
//        if (this.mybatisXmlMapperAutoReLoader == null) {
//            afterPropertiesSet();
//        }
//        return this.mybatisXmlMapperAutoReLoader;
//    }
//
//    @Override
//    public void destroy() throws Exception {
//        if (this.mybatisXmlMapperAutoReLoader != null) {
//            this.mybatisXmlMapperAutoReLoader.destroy();
//        }
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        this.mybatisXmlMapperAutoReLoader = new MybatisXmlMapperReloaderFactory();
//        this.mybatisXmlMapperAutoReLoader.setEnableAutoReload(this.enableAutoReload);
//        this.mybatisXmlMapperAutoReLoader.setSqlSession(this.sqlSession);
//        this.mybatisXmlMapperAutoReLoader.init();
//    }
//
//    @Override
//    public Class<?> getObjectType() {
//        return MybatisXmlMapperReloaderFactory.class;
//    }
//
//    @Override
//    public boolean isSingleton() {
//        return true;
//    }
//
//    public void setEnableAutoReload(boolean enableAutoReload) {
//        this.enableAutoReload = enableAutoReload;
//    }
//
//    public void setSqlSession(SqlSession sqlSession) {
//        this.sqlSession = sqlSession;
//    }
//
//}
