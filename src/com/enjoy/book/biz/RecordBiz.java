package com.enjoy.book.biz;

import com.enjoy.book.bean.Book;
import com.enjoy.book.bean.Member;
import com.enjoy.book.bean.Record;
import com.enjoy.book.dao.BookDao;
import com.enjoy.book.dao.MemberDao;
import com.enjoy.book.dao.RecordDao;
import com.enjoy.book.util.DBHelper;
import com.enjoy.book.util.DateHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RecordBiz {
    RecordDao  recordDao = new RecordDao();
    BookDao  bookDao = new BookDao();
    MemberDao memberDao = new MemberDao();
    MemberBiz  memberBiz = new MemberBiz();
    public List<Record> getRecordsByIdNum(String idNum){
        List<Record> records = null;
        try {
            records=recordDao.getRecordsByIdNum(idNum);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return records;
    }
    public List<Record> getRecordsByMemberId(long memberId){
        List<Record> records = null;
        try {
            records=recordDao.getRecordsByMemberId(memberId);
            //1.外键信息
            //1.1 获取会员对象
           // Member member = memberDao.getById(memberId);//拿不到外键对象
            Member member = memberBiz.getById(memberId);
            for(Record record:records){
                long bookId = record.getBookId();
                Book book = bookDao.getById(bookId);
                record.setBook(book);
                record.setMember(member);
                //2.应还时间 ,借阅时间+keeyDay
                long day = member.getType().getKeepDay();
                //做时间的计算(Java)
                java.sql.Date  rentDate = record.getRentDate();
                //转成毫秒数
                java.sql.Date backDate = DateHelper.getNewDate(rentDate,day);
                record.setBackDate(backDate);
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return records;
    }

    /**
     * 借阅:
     * 1.借一本数： record表添加一行信息(recordDao,insert) 1
     * 2.这本书的数量-1 (bookDao,update) 1
     * 3.会员信息表中 ,更改余额( memberDao,update) 1
     * 要么全部成功，要么全部失败(一个业务(事务处理))
     * 前提:用同一个connection对象(如何?)
     * @param memberId
     * @param bookIdList
     * @param userId
     * @return 0:操作失败  1:操作成功
     */
    public int add(long memberId,List<Long> bookIdList,long userId){
        try {
            //1.启动事务
            DBHelper.beginTransaction();
            double total = 0;
            //2.拿到借阅的书籍编号
            for(long bookId: bookIdList){
                //书籍编号

                //书籍对象
                Book book = bookDao.getById(bookId);
                //调用价格
                double price = book.getPrice();
                double regPrice = price*0.3f;
                total += regPrice;
                //算押金
                //调用recordDao-->insert
                recordDao.add(memberId,bookId,regPrice,userId);
                //调用bookDao --> update 数量
                bookDao.modify(bookId,-1);

            }
            //调用memberDao -->update 余额
            memberDao.modifyBalance(memberId,0-total);
            //.事务结束:
            DBHelper.commitTransaction();//事务提交:成功

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                DBHelper.rollbackTransaction();//事务回滚:有异常
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        return 1;

    }

    /**
     * 归还功能
     * 开启事务
     * 1.recordDao modify: 押金，归还日期，操作员的编号
     * 2.memberDao modify: 余额
     * 3.书籍信息表  modify :数量
     * 成功：提交
     * 失败:回滚
     *
     * @param memberId
     * @param recordIds
     * @param userId
     * @return
     */
    public int  modify(long memberId, List<Long> recordIds, long userId) {
        //1.开启事务
        try {
            DBHelper.beginTransaction();
            double total = 0;
            Member member = memberBiz.getById(memberId);
            for(long recordId:recordIds){
                //2.2 通过recordId 获取记录对象:书
               Record record = recordDao.getById(recordId);
               //2.1 先累加押金
                //计算押金(逾期:超出1天扣1块)
                java.sql.Date  backDate = DateHelper.getNewDate(record.getRentDate(),member.getType().getKeepDay());
                //系统当前时间
                java.util.Date currentDate = new java.util.Date();
                int day = 0;
                if(currentDate.after(backDate)){
                    //计算押金
                     day = DateHelper.getSpan(currentDate,backDate);
                }
               total += record.getDeposit()-day;
               //2.2 更改record
                recordDao.modify(day,userId,recordId);
                //2.3 修改书籍 +1
                bookDao.modify(record.getBookId(),1);
            }
            //2。4修改余额
            memberDao.modifyBalance(memberId,total);
            DBHelper.commitTransaction();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                DBHelper.rollbackTransaction();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        return 1;

    }
    public  int modify(long id){
        int count =0;
        try {
            count = recordDao.modify(id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return count;


    }

    /**
     * 查询
     * @param typeId
     * @param keyword
     * @return
     */
    public List<Map<String,Object>> query(int typeId, String keyword){
        List<Map<String,Object>> rows = null;
        try {
            rows = recordDao.query(typeId,keyword);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rows;

    }
}
