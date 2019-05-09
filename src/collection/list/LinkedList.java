/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util;

import java.util.function.Consumer;


public class LinkedList<E>
        extends AbstractSequentialList<E>
        implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{

    //记录链表的节点个数
    transient int size = 0;


    //头节点
    transient Node<E> first;


    //末尾节点
    transient Node<E> last;


    public LinkedList() {
    }

    //新建一个包含传入集合中所有元素的LinkedList
    public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    //生成新节点，值为e，并且插入到原链表中头节点前面
    private void linkFirst(E e) {
        //原头节点
        final Node<E> f = first;
        //生成新节点，前驱节点指向为null，后继节点为原头节点
        //头节点特征就是前驱节点指向null
        final Node<E> newNode = new Node<>(null, e, f);
        //更新头节点为新生成的节点
        first = newNode;
        //如果原头节点为null，即链表为空，则设置末尾节点也为新节点
        if (f == null)
            last = newNode;
        //若头节点不为空，则将原本头节点的前驱节点指向新节点，即将原头节点变为第二个节点
        else
            f.prev = newNode;
        //节点个数加一
        size++;
        //fail-fast机制
        modCount++;
    }

    //生成新的节点，值为e，并且插入在原链表中末尾节点后面
    void linkLast(E e) {
        //原末尾节点
        final Node<E> l = last;
        //生成新节点，其前驱节点指向原末尾节点，值为e，后继节点指向为null
        final Node<E> newNode = new Node<>(l, e, null);
        //更新末尾节点为新生成的节点
        last = newNode;
        //如果末尾节点为null，即链表为空，则将头节点也设置为新生成的节点
        if (l == null)
            first = newNode;
        //若末尾节点不为null，则将原末尾节点的后继节点指向新节点，即原末尾节点变为倒数第二个节点
        else
            l.next = newNode;
        //节点个数加一
        size++;
        modCount++;
    }

    //生成新节点，值为e，并且插入在链表中succ节点的前面
    void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
        //记录传入节点的前驱节点
        final Node<E> pred = succ.prev;
        //生成新节点，其前驱节点指向传入节点的前驱节点，值为e，后继节点指向传入节点
        //实际就是将新节点插入到传入节点succ和传入节点succ的前驱节点之间
        final Node<E> newNode = new Node<>(pred, e, succ);
        //更新传入节点succ的前驱节点指向新节点
        succ.prev = newNode;
        //若传入节点succ的前驱节点为null，则证明succ节点为头节点，则更新头节点为新节点
        if (pred == null)
            first = newNode;
        //如果传入节点succ的前驱节点不为null，则将节点succ的前驱节点的后继节点指向新节点
        else
            pred.next = newNode;
        size++;
        modCount++;
    }

    /**
     * Unlinks non-null first node f.
     */
    //断开头节点连接，传入节点f为头节点且不为null
    private E unlinkFirst(Node<E> f) {
        // assert f == first && f != null;
        //原头节点节点值
        final E element = f.item;
        //原头节点的后继节点
        final Node<E> next = f.next;
        //断开头节点的连接
        f.item = null;
        f.next = null; // help GC
        //更新头节点为原头节点的后继节点
        first = next;
        //若现头节点为null，即原链表只有一个节点，则将末尾节点更新为null
        if (next == null)
            last = null;
        //若现头节点不为null，则将现头节点的前驱节点指向null，符合头节点定义
        //由null<-f<-next变为null<-next
        else
            next.prev = null;
        size--;
        modCount++;
        //返回原头节点值
        return element;
    }

    /**
     * Unlinks non-null last node l.
     */
    //断开末尾节点连接，传入节点l为末尾节点且不为null
    private E unlinkLast(Node<E> l) {
        // assert l == last && l != null;
        //原末尾节点值
        final E element = l.item;
        //原末尾节点的前驱节点
        final Node<E> prev = l.prev;
        //断开原末尾节点连接
        l.item = null;
        l.prev = null; // help GC
        //更新末尾节点为原末尾节点的前驱节点
        last = prev;
        //若现末尾节点为null，则证明原链表中只有一个元素，则将头节点更新为null
        if (prev == null)
            first = null;
        //若原末尾节点的前驱节点不为null，则将现末尾节点的后继节点指向null，符合末尾节点定义
        //由prev->l->null变为prev->null
        else
            prev.next = null;
        size--;
        modCount++;
        //返回原末尾节点的值
        return element;
    }

    /**
     * Unlinks non-null node x.
     */
    //断开节点x的连接，传入节点x不为null
    E unlink(Node<E> x) {
        // assert x != null;
        //要断开连接的节点的节点值
        final E element = x.item;
        //要断开连接的节点的后继节点
        final Node<E> next = x.next;
        //要断开连接的节点的前驱节点
        final Node<E> prev = x.prev;

        //若节点x的前驱节点为null，则证明要断开连接的节点为头节点，则将头节点更新为节点x的后继节点
        if (prev == null) {
            first = next;
        //若节点x前驱节点不为null
        } else {
            //将节点x的前驱节点的后继节点指向节点x的后继节点（这时prev->x->next变为prev->next）
            prev.next = next;
            //将节点x的前驱节点指向为null
            x.prev = null;
        }

        //若节点x的后继节点为null，则证明要断开的节点为末尾节点，则将末尾节点更新为节点x前驱节点
        if (next == null) {
            last = prev;
        //若节点x的后继节点不为null
        } else {
            //将节点x的后继节点的前驱节点指向节点x的前驱节点（这时prev<-x<-next变为prev<-next）
            next.prev = prev;
            //将节点x的后继节点指向null（这时null<-x->null,即已将节点x断开）
            x.next = null;
        }

        x.item = null;//help GC
        size--;
        modCount++;
        //返回断开节点的节点值
        return element;
    }

    //返回链表中头节点的值，若链表为空，抛出异常
    public E getFirst() {
        //头节点
        final Node<E> f = first;
        //若头节点为空，即链表为空，抛出异常
        if (f == null)
            throw new NoSuchElementException();
        //返回头节点的值
        return f.item;
    }

    //返回链表中末尾节点的值，若链表为空，抛出异常
    public E getLast() {
        //末尾节点
        final Node<E> l = last;
        //若末尾节点为空，即链表为空，抛出异常
        if (l == null)
            throw new NoSuchElementException();
        //返回末尾节点的值
        return l.item;
    }

    //删除头节点，若链表为空，抛出异常
    public E removeFirst() {
        //头节点
        final Node<E> f = first;
        //若头节点为空，即链表为空，则抛出异常
        if (f == null)
            throw new NoSuchElementException();
        //断开头节点连接，并返回原头节点的值
        return unlinkFirst(f);
    }

    //删除末尾节点，若链表为空，抛出异常
    public E removeLast() {
        //末尾节点
        final Node<E> l = last;
        //若末尾节点为空，即链表为空，则抛出异常
        if (l == null)
            throw new NoSuchElementException();
        //断开末尾节点的连接，并返回原末尾节点的值
        return unlinkLast(l);
    }

    //添加新节点，并把新节点添加在原链表头节点之前
    public void addFirst(E e) {
        linkFirst(e);
    }

    //添加新节点，并把新节点添加在原链表末尾节点之后
    public void addLast(E e) {
        linkLast(e);
    }

    //用indexOf方法检查列表中是否包含值与对象o相等的节点
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    //返回链表的实际节点个数
    public int size() {
        return size;
    }

    //添加新节点，并放置于原末尾节点之后
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    //删除链表中出现的第一个值与对象o相等的节点
    public boolean remove(Object o) {
        //传入值为null
        if (o == null) {
            //顺序遍历链表
            for (Node<E> x = first; x != null; x = x.next) {
                //当有节点值等于null时，调用unlink方法断开该节点连接，并直接返回true
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            //若传入值不为null，顺序遍历链表
            for (Node<E> x = first; x != null; x = x.next) {
                //当有节点值与传入值o相等时，调用unlink方法断开该节点连接，并直接返回true
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        //若链表中无节点的值与传入值相等，即未删除节点，则返回false
        return false;
    }

    //将传入集合中的元素添加在原链表末尾节点之后
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    //将传入集合中的元素添加在指定位置节点之前
    public boolean addAll(int index, Collection<? extends E> c) {
        //检查传入索引是否符合规则
        checkPositionIndex(index);

        //将传入集合转换为数组
        Object[] a = c.toArray();
        //集合元素个数
        int numNew = a.length;
        //若传入集合元素个数为0，则直接返回false
        if (numNew == 0)
            return false;

        //succ用来记录要插入位置的节点，pred用来记录接下来要用来做插入节点的前驱节点的节点
        Node<E> pred, succ;
        //若传入位置值等于节点个数，则要插入位置为末尾节点之后，即succ为null，pred为末尾节点
        if (index == size) {
            succ = null;
            pred = last;
        //若不等于节点个数，则是要插入到指定位置之前，则succ为index位置节点，pred为succ的前驱节点
        } else {
            succ = node(index);
            pred = succ.prev;
        }

        //遍历集合转换而来的数组a
        for (Object o : a) {
            @SuppressWarnings("unchecked") E e = (E) o;
            //生成新节点，其前驱节点为pred，值为e，后继节点在下一次循环中更新
            Node<E> newNode = new Node<>(pred, e, null);
            //若记录的前驱节点为null，则证明插入位置为头节点位置，则更新头节点为新节点
            if (pred == null)
                first = newNode;
            //若记录的前驱节点不为null，则更新前驱节点的后继节点为新节点
            //这一步实际上是更新前一个插入的新节点的后继节点为现在插入的新节点
            else
                pred.next = newNode;
            //更新记录接下来要做插入新节点的前驱节点的节点为新节点
            pred = newNode;
        }

        //若succ等于null，则证明是将元素插入到原末尾节点之后，则将末尾节点更新为最后一个插入的节点
        if (succ == null) {
            last = pred;
        //若不等于null，则将最后一个插入的节点的后继节点指向succ节点，将succ节点的前驱节点更新为最后一个插入的节点
        } else {
            pred.next = succ;
            succ.prev = pred;
        }

        size += numNew;
        modCount++;
        //插入成功则返回true
        return true;
    }

    //清空链表，将链表中所有节点删除
    public void clear() {
        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        //   more than one generation
        // - is sure to free memory even if there is a reachable Iterator
        //循环遍历链表，清空节点值和前驱节点和后继节点，达到删除节点的目的
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        //更新头节点和末尾节点为null
        first = last = null;
        //更新节点个数为0
        size = 0;
        modCount++;
    }


    // Positional Access Operations

    //获取指定位置的节点的值
    public E get(int index) {
        //检查传入索引是否符合规则
        checkElementIndex(index);
        //返回指定位置的节点的节点值
        return node(index).item;
    }

    //更新指定位置节点的值，并返回旧值
    public E set(int index, E element) {

        checkElementIndex(index);
        Node<E> x = node(index);
        //获取节点的旧值
        E oldVal = x.item;
        //将节点值更新为传入的新值
        x.item = element;
        //返回旧值
        return oldVal;
    }

    //在指定位置添加新节点
    public void add(int index, E element) {
        checkPositionIndex(index);

        //若传入索引等于节点个数，则直接生成新节点插入到原末尾节点之后
        if (index == size)
            linkLast(element);
        //反之，则生成新节点插入到指定位置节点之前
        else
            linkBefore(element, node(index));
    }

    //删除指定位置节点
    public E remove(int index) {
        checkElementIndex(index);
        //断开指定位置节点连接，并返回该节点值
        return unlink(node(index));
    }

    //判断传入索引是否是节点索引
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    //判断传入索引是否是节点索引（包含size）
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    //生成异常信息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    //检查传入索引是否是节点索引，不是就抛出异常
    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    //检查传入索引是否是节点索引（包含size），不是就抛出异常
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    //获取指定索引的节点
    Node<E> node(int index) {
        // assert isElementIndex(index);
        //判断传入索引是大于还是小于节点个数的一半，若小于size的1/2，则顺序遍历获得值
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        //若大于size的1/2，则倒序遍历获得值，这样大大提高了速度
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    // Search Operations

    //返回传入对象在链表中第一次出现的位置
    public int indexOf(Object o) {
        //记录位置
        int index = 0;
        if (o == null) {
            //若传入对象o为null，则顺序遍历链表。
            for (Node<E> x = first; x != null; x = x.next) {
                //当有节点值等于null时，结束遍历，直接返回index
                if (x.item == null)
                    return index;
                //记录位置的值累加
                index++;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item))
                    return index;
                index++;
            }
        }
        //若链表中不包含传入对象，则返回-1
        return -1;
    }

    //返回链表中传入对象最后一次出现的位置
    public int lastIndexOf(Object o) {
        //设置记录位置的值为节点个数
        int index = size;
        if (o == null) {
            //若传入对象o为null，则倒序遍历链表
            for (Node<E> x = last; x != null; x = x.prev) {
                //不断累减记录位置值
                index--;
                //当有节点值等于null时，结束遍历，直接返回index
                if (x.item == null)
                    return index;
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item))
                    return index;
            }
        }
        //若链表中不包含传入对象，则返回-1
        return -1;
    }

    // Queue operations.

    //返回链表头节点的值，若链表为空，则返回null。
    public E peek() {
        //头节点
        final Node<E> f = first;
        //若头节点为null，即链表为空，则直接返回null；反之，返回头节点的节点值
        return (f == null) ? null : f.item;
    }

    //返回头节点的值，若链表为空，则抛出异常
    public E element() {
        return getFirst();
    }

    //删除头节点，并返回原头节点的值。若链表为空，则返回null。符合先进先出队列
    public E poll() {
        //头节点
        final Node<E> f = first;
        //若头节点为null，即链表为空，则直接返回null；反之，删除头节点，并返回删除节点的值
        return (f == null) ? null : unlinkFirst(f);
    }

    //删除链表的头节点。若链表为空，则抛出异常
    public E remove() {
        return removeFirst();
    }

    //生成新节点，值为e，并将新节点插入在原链表末尾节点之后
    public boolean offer(E e) {
        return add(e);
    }

    // Deque operations
    //生成新节点，值为e，并将新节点插入到原链表头节点之前
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    //生成新节点，值为e，并将新节点插入到原链表末尾节点之后
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    //返回链表头节点的值，若链表为空，则返回null
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    //返回链表末尾节点的值，若链表为空，则返回null
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

    //删除链表头节点，并返回头节点的值。若链表为空，直接返回null
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    //删除链表末尾节点，并返回末尾节点的值。若链表为空，直接返回null
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }

    //生成新节点，值为e，并插入到原链表头节点之前
    public void push(E e) {
        addFirst(e);
    }

    //删除头节点，符合队列的先进先出
    public E pop() {
        return removeFirst();
    }

    //删除链表中第一次出现与传入值相等的节点
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    //删除链表中最后一次出现与传入值相等的节点
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            //当传入对象o为null时，倒序遍历链表
            for (Node<E> x = last; x != null; x = x.prev) {
                //当有节点值等于null时，断开该节点连接，并结束遍历，直接返回true
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        //若链表中无节点值与传入对象o相等，即无删除节点，则直接返回false
        return false;
    }

    //返回一个从指定位置开始的列表迭代器
    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }

    private class ListItr implements ListIterator<E> {
        //记录刚被迭代的节点
        private Node<E> lastReturned;
        //记录下一个要迭代的节点
        private Node<E> next;
        //记录下一个要迭代的节点的位置
        private int nextIndex;
        private int expectedModCount = modCount;

        ListItr(int index) {
            // assert isPositionIndex(index);
            //若传入位置等于节点个数，则下一个要迭代的节点未null；反之，为index位置节点
            next = (index == size) ? null : node(index);
            //下一个要迭代的节点的位置
            nextIndex = index;
        }

        //如果下一个要迭代的节点的位置值小于链表节点个数，则证明节点未被迭代
        public boolean hasNext() {
            return nextIndex < size;
        }

        //迭代元素
        public E next() {
            //fail-fast机制
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();

            //更新刚被迭代过的节点为next
            lastReturned = next;
            //下一个要迭代的节点为next的后继节点
            next = next.next;
            //下一个要迭代的节点位置值加一
            nextIndex++;
            //返回刚被迭代过的节点的值
            return lastReturned.item;
        }

        //如果下一个要迭代的节点的位置值大于0，则证明有前一个元素
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        //往前迭代，返回下一个要迭代节点的前一个节点值，并更新下一个要迭代的节点为前一个节点
        public E previous() {
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();

            /*
                若下一个要迭代的值为null，则证明当前位置已经时末尾节点了
                则将位置前移至倒数第二个节点，则下一个要迭代的元素就为末尾节点
                反之，更新下一个要迭代的节点为原下一个要迭代节点的前驱节点
            */
            lastReturned = next = (next == null) ? last : next.prev;
            //下一个要迭代节点位置前移
            nextIndex--;
            return lastReturned.item;
        }

        //返回下一个要迭代的节点的位置
        public int nextIndex() {
            return nextIndex;
        }

        //返回下一个要迭代的节点的前一个节点的位置
        public int previousIndex() {
            return nextIndex - 1;
        }

        //删除刚被迭代过的节点
        public void remove() {
            checkForComodification();
            if (lastReturned == null)
                throw new IllegalStateException();

            //记录刚被迭代过节点的后继节点
            Node<E> lastNext = lastReturned.next;
            //断开刚被迭代过的节点的连接
            unlink(lastReturned);
            //若下一个要迭代的元素为刚被迭代过的元素（即使用previous方法之后）
            //将下一个要迭代的节点更新为刚被迭代过的节点的后继节点
            if (next == lastReturned)
                next = lastNext;
            else
                //若不相等，则直接将要迭代的节点的位置前移。因为删除节点之后，要迭代的节点的位置也会往前移
                nextIndex--;
            //将记录刚被迭代过的元素的值设置为null
            lastReturned = null;
            expectedModCount++;
        }

        //更新刚被迭代过的节点的值
        public void set(E e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.item = e;
        }

        //生成新节点，节点值为e，并将新节点插入到下一个要迭代的节点前面
        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            //若下一个要迭代的节点为null，即当前为末尾节点时，将新节点插入到原末尾节点之后
            if (next == null)
                linkLast(e);
            //若当前节点不为末尾节点，则将新节点插入到下一个要迭代的节点位置之前
            else
                linkBefore(e, next);
            //因为下一个要迭代的节点前多了一个节点，所以要将记录下一个要迭代的节点的位置值加一
            nextIndex++;
            expectedModCount++;
        }

        //对迭代器中剩余未被迭代元素进行传入规则操作
        public void forEachRemaining(Consumer<? super E> action) {
            //检查传入规则是否为null，是则抛出异常
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                //对节点的值进行规则应用
                action.accept(next.item);
                //更新刚被迭代过的值
                lastReturned = next;
                //更新下一个要迭代的节点
                next = next.next;
                //更新下一个要迭代的节点的位置
                nextIndex++;
            }
            checkForComodification();
        }

        //fail-fast机制
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    //节点内部类，这就是LinkedList的数据存储结构（底层）
    private static class Node<E> {
        //记录节点值
        E item;
        //指向下一个节点
        Node<E> next;
        //指向前一个节点
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    //返回一个从后往前遍历的迭代器
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    //一个从后往前的列表迭代器
    private class DescendingIterator implements Iterator<E> {
        //构造一个起始位置在最后一个节点的列表迭代器
        private final ListItr itr = new ListItr(size());
        //判断是否有下一个节点，实际上是在判断是否是第一个节点（因为是从后往前遍历）
        public boolean hasNext() {
            return itr.hasPrevious();
        }
        //next方法实际上就是previous方法，返回前一个节点
        public E next() {
            return itr.previous();
        }
        public void remove() {
            itr.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedList<E> superClone() {
        try {
            return (LinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    //克隆LinkedList
    public Object clone() {
        LinkedList<E> clone = superClone();

        // Put clone into "virgin" state
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;

        // Initialize clone with our elements
        for (Node<E> x = first; x != null; x = x.next)
            clone.add(x.item);

        return clone;
    }

    //生成一个存有链表中所有节点值的数组
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;
        return result;
    }

    //将链表中元素转存至指定泛型数组中
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        //若传入泛型数组的长度小于链表节点个数，则用反射机制新建一个长度为size的数组
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        int i = 0;
        Object[] result = a;
        //遍历链表，将链表中的节点值依次放入数组中
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;

        if (a.length > size)
            a[size] = null;

        return a;
    }

    private static final long serialVersionUID = 876323262645176354L;

    //私有方法，将LinkedList实例序列化
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (Node<E> x = first; x != null; x = x.next)
            s.writeObject(x.item);
    }

    //私有方法，从反序列化中重构LinkedList实例
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in size
        int size = s.readInt();

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++)
            linkLast((E)s.readObject());
    }

    //返回一个链表分割器
    @Override
    public Spliterator<E> spliterator() {
        return new LLSpliterator<E>(this, -1, 0);
    }

    /** A customized variant of Spliterators.IteratorSpliterator */
    static final class LLSpliterator<E> implements Spliterator<E> {
        //实际上是2的10次方，最大的批处理数组的增量
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
        //实际上是2的25次方，最大的批处理数组的容量
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        //放置链表实例
        final LinkedList<E> list; // null OK unless traversed
        //当前节点
        Node<E> current;      // current node; null until initialized
        //记录未被操作节点个数，若等于-1，则是所有节点都未被操作
        int est;              // size estimate; -1 until first needed
        int expectedModCount; // initialized when est set
        //每次批处理长度
        int batch;            // batch size for splits

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        //获取链表中未被分割器操作的节点个数
        final int getEst() {
            //用来记录分割器中未被操作节点个数
            int s; // force initialization
            final LinkedList<E> lst;
            //将est赋值给s，同时，若est小于0，即链表中节点都未被分割器操作
            //若链表为空，则需要操作节点也就为0
            //若链表不为空，则所需操作节点个数为链表节点个数size
            //因为都未被操作，所有将当前节点设置为头节点
            if ((s = est) < 0) {
                if ((lst = list) == null)
                    s = est = 0;
                else {
                    expectedModCount = lst.modCount;
                    current = lst.first;
                    s = est = lst.size;
                }
            }
            //若est不小于0，证明已经有节点被操作过，则无需修改current为头节点，直接返回即可
            return s;
        }

        //返回链表中未被分割器操作过的节点的个数
        public long estimateSize() { return (long) getEst(); }

        public Spliterator<E> trySplit() {
            Node<E> p;
            int s = getEst();
            if (s > 1 && (p = current) != null) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j++] = p.item; } while ((p = p.next) != null && j < n);
                current = p;
                batch = j;
                est = s - j;
                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
            }
            return null;
        }

        //对分割器中剩余的未被操作过的节点进行传入规则操作
        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p; int n;
            if (action == null) throw new NullPointerException();
            if ((n = getEst()) > 0 && (p = current) != null) {
                //将当前节点设置为null
                current = null;
                //将记录未被操作过元素个数设置为0
                est = 0;
                //对剩余节点进行遍历，并调用accept方法对节点值进行规则操作
                do {
                    E e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

        //对链表中当前节点进行传入规则操作，并将当前节点指向下一个节点
        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            if (getEst() > 0 && (p = current) != null) {
                //未被操作过的节点数减一
                --est;
                //获取当前节点值
                E e = p.item;
                //将记录下一个要被操作节点指向下一个节点
                current = p.next;
                //对节点值进行操作
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

}
