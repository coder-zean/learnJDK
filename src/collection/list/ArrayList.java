
package java.util;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import sun.misc.SharedSecrets;


public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 8683452581122892189L;

    //表示ArrayList的默认容量为10
    private static final int DEFAULT_CAPACITY = 10;

    //无元素数组
    private static final Object[] EMPTY_ELEMENTDATA = {};

    //这个数组表示默认容量数组，但是长度为0，
    //只有在第一次add的时候，数组的长度才会被扩容为默认容量10
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};


    //这个成员变量用来存储元素
    //transient关键字表示序列化时不序列化该字段
    transient Object[] elementData; // non-private to simplify nested class access

    //记录ArrayList元素个数
    private int size;


    //传入int类型变量时，创建一个初始容量为initialCapacity、空的ArrayList
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                    initialCapacity);
        }
    }

    //不传参构造时，构建一个初始容量为默认容量10、空的ArrayList
    //（第一次add时，elementData才会被扩容为默认容量10）
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    //传入一个集合时，创建一个包含collection的所有元素的ArrayList
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // replace with empty array.
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }

    //将ArrayList中存储元素的数组大小进行修剪，使其长度等于实际元素个数size
    public void trimToSize() {
        //修改次数加一，这里时fast-fail机制
        modCount++;
        //如果ArrayList的实际元素个数小于存储元素数组长度，则对数组进行修剪
        if (size < elementData.length) {
            //若实际元素个数为0，则将对象存储数组设置为空数组
            //反之，将原数组复制至一个长度为实际元素个数的新数组中
            elementData = (size == 0)
                    ? EMPTY_ELEMENTDATA
                    : Arrays.copyOf(elementData, size);
        }
    }


    //对存储元素的数组进行扩容，minCapacity是当前ArrayList最小所需容量
    public void ensureCapacity(int minCapacity) {
        //只要存储元素数组不是默认长度数组，则最小花费都为0，反之都为默认容量10
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
                // any size if not default element table
                ? 0
                // larger than default for default empty table. It's already
                // supposed to be at default size.
                : DEFAULT_CAPACITY;

        //如果最小所需容量的大于最小花费容量，则进行扩容
        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    //计算ArrayList当前最小所需容量
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        /*
            如果数组等于定义的默认容量数组，则返回默认容量10和传入最小所需容量的最大值
            这个操作是为了让我们用无参构造方法创建ArrayList的时候，
            elementData被赋予DEFAULTCAPACITY_EMPTY_ELEMENTDATA，
            虽然字面意思为默认容量数组，但是实际长度为0，
            所以我们就需要在第一次add的时候，将这个数组的长度真正扩大到默认容量10
        */
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }

    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }

    //判断是否需要进行扩容，需要则用grow方法进行扩容
    private void ensureExplicitCapacity(int minCapacity) {
        //修改次数加一，add操作时，对modCount域操作实际在这一步
        modCount++;

        // overflow-conscious code
        //如果传入最小所需容量大于存储元素数组的长度，则执行grow方法对数组进行扩容
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    /**
     * 数组缓冲区最大存储容量
     * - 一些 VM 会在一个数组中存储某些数据--->为什么要减去 8 的原因
     * - 尝试分配这个最大存储容量，可能会导致 OutOfMemoryError(当该值 > VM 的限制时)
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;


    //扩容算法，数组长度扩大50%，以保证能装下minCapacity个元素
    private void grow(int minCapacity) {
        // overflow-conscious code
        //记录未扩容前容量
        int oldCapacity = elementData.length;
        //oldCapacity>>1实际时oldCapacity的二分之一向下取整，所以新容量比旧容量大百分50
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        //如果这个扩容50%后的容量仍下于最小所需容量，则将新容量设置为最小所需容量
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        //如果新容量大于数组缓冲区最大存储容量，则用hugeCapacity最大化容量
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        //将原数组复制到长度为新容量的新数组中
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    //对存储元素数组进行大容量分配，最大分配 Integer.MAX_VALUE
    private static int hugeCapacity(int minCapacity) {
        //如果最小所需容量小于0，则抛出异常
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        //如果最小所需容量大于Integer.MAX_VALUE-8,则返回Integer.MAX_VALUE
        //反之，返回Integer.MAX_VALUE-8
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    //返回ArrayList中实际存储的元素个数
    public int size() {
        return size;
    }

    //判断ArrayList中是否有存储元素
    public boolean isEmpty() {
        return size == 0;
    }

    //用indexOf方法判断ArrayList中是否包含传入元素
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    //获取元素在ArrayList中第一次出现的位置
    public int indexOf(Object o) {
        //如果传入元素等于null，则对数组进行顺序遍历，当有元素等于null时，返回下标。
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        //如果传入元素不等于null，则对数组进行顺序遍历，当有元素与传入元素相等时，返回下标。
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        //若ArrayList中没有改元素，直接返回-1
        return -1;
    }

    //返回传入元素在ArrayList中最后一次出现的位置
    public int lastIndexOf(Object o) {
        //操作和indexOf方法类似，只是indexOf方法时顺序遍历，而该方法时倒序遍历
        if (o == null) {
            for (int i = size-1; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = size-1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    //克隆ArrayList
    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    //返回ArrayList中存储元素的数组的复制，大小为size
    public Object[] toArray() {
        //返回 Arrays.copyOf(elementData, size)是为了去除数组中未存放元素的位置
        return Arrays.copyOf(elementData, size);
    }

    //返回存储元素的指定类型数组
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        //如果传入数组的长度小于ArrayList元素个数
        //则返回长度为size，类型为T的elementData数组的复制
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        //将elementData数组0到size-1索引的元素复制到a中
        System.arraycopy(elementData, 0, a, 0, size);
        //如果数组a的长度大于size，则将a的size索引位置的元素赋值为null
        if (a.length > size)
            a[size] = null;
        return a;
    }

    //返回index索引位置元素
    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    //返回index索引位置元素
    public E get(int index) {
        //检查传入索引是否符合规则
        rangeCheck(index);

        return elementData(index);
    }

    //将传入索引位置的元素变成新传入的元素，并返回旧元素
    public E set(int index, E element) {
        rangeCheck(index);

        //获取index位置未修改前的旧元素
        E oldValue = elementData(index);
        //用新元素覆盖旧元素
        elementData[index] = element;
        //返回旧元素
        return oldValue;
    }

    //新增元素在所有元素后面
    public boolean add(E e) {
        //判断元素是否需要扩容，modCount域在这个方法里会加一
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        //将新元素放在所有元素后面，并将记录实际元素个数的值size加一
        elementData[size++] = e;
        return true;
    }

    //将新元素插入到指定索引位置
    public void add(int index, E element) {
        //检查传入索引是否符合规则
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);  // Increments modCount!!
        /*
            将数组中index位置元素到size-1位置元素复制一份，并放到数组index+1开始位置。
            实际上就是elementData[index]->elementData[index+(size-index)-1]元素复制，
            放到elementData[index+1]->elementData[(index+1)+(size-index)-1]位置上。
            这时elementData[index]==elementData[index+1],就将index位置之后元素都往后移动了一位，
            最后再用传入的新元素覆盖element[index]的值，达到插入指定位置的目的
        */
        System.arraycopy(elementData, index, elementData, index + 1,
                size - index);
        elementData[index] = element;
        size++;
    }

    //删除指定位置元素，并返回被删除的元素
    public E remove(int index) {
        rangeCheck(index);

        modCount++;
        E oldValue = elementData(index);

        //计算删除元素后，所需往前移动的元素个数
        int numMoved = size - index - 1;
        if (numMoved > 0)
            //将elementData[index+1]->elementData[(index+1)+numMoved-1]位置元素复制，
            //放到element[index]->element[index+numMoved-1]位置上，
            //这样就把element[index]值覆盖删除，同时后面元素都往前移动
            System.arraycopy(elementData, index+1, elementData, index,
                    numMoved);
        //将element[size-1]位置元素赋值null，同时记录元素个数值size减一
        elementData[--size] = null; // clear to let GC do its work

        return oldValue;
    }

    //删除传入指定元素，且只删除索引值最小的哪个
    public boolean remove(Object o) {
        //若传入值为null，则对数组进行遍历，当有元素等于null时，用fastRemove方法删除并返回true
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        //若没有元素等于传入值，则返回false
        return false;
    }

    //fastRemove方法与remove(int index)方法类似
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                    numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }

    //清空ArrayList中所有元素
    public void clear() {
        modCount++;

        // clear to let GC do its work
        //遍历数组，将所有值都赋值为null
        for (int i = 0; i < size; i++)
            elementData[i] = null;
        //将记录元素个数的值赋值为0
        size = 0;
    }

    //将传入集合中的所有元素添加到ArrayList中
    public boolean addAll(Collection<? extends E> c) {
        //将集合中元素放在数组中
        Object[] a = c.toArray();
        //新元素个数
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // Increments modCount
        //将a[0]->a[0+numNew-1]元素复制放在elementData[size]开始位置
        //即将传入集合中所有元素放在ArrayList所有元素后面
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        //如果传入集合有元素，则返回true;反之，返回false
        return numNew != 0;
    }

    //将传入集合中所有元素插入到ArrayList指定位置
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);

        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // Increments modCount

        //记录插入集合中所有元素后，需要移动原有元素个数
        int numMoved = size - index;
        if (numMoved > 0)
            //将elementData[index]->element[index+numMoved-1]位置元素复制，
            //放到elementData[index+numNew]开始位置上
            System.arraycopy(elementData, index, elementData, index + numNew,
                    numMoved);

        //将a[0]->a[0+numNew-1]位置元素复制，
        //放到elementData[index]开始位置上,
        //这样就覆盖了index到index+numNew之间的值，达到插入目的
        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }

    //删除从开始坐标fromIndex到截止坐标toIndex
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                numMoved);

        // clear to let GC do its work
        int newSize = size - (toIndex-fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }

    //检查传入下标是否超过ArrayList容量
    private void rangeCheck(int index) {
        //若下标超出容量，抛出异常
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    //检查添加元素时，传入下标是否大于容量或者小于0
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    //返回异常信息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    //删除ArrayList中与传入集合中元素相同的元素
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }

    //只保留ArrayList中与传入集合中元素相同的元素
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }

    //删除指定元素
    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        //r记录存储元素数组下标，w记录要保留元素的个数
        int r = 0, w = 0;
        boolean modified = false;
        try {
            /*
                当complement为false时，即为保留c中不包含的元素，
                然后将元素不断前移覆盖，记录保留数目的变量加一，
                这样就达到，数组前w+1个数是要保留的元素;反之，类推。
            */
            for (; r < size; r++)
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
            //保留与AbstractCollection的行为兼容性，防止c.contains（）引发异常导致的操作停止。
            if (r != size) {
                //将下标r之后的所有元素复制往前移动到下标w保留元素位置之后
                System.arraycopy(elementData, r,
                        elementData, w,
                        size - r);
                //将记录保留数加上下标r之后所有元素个数
                //这样保证异常发生之后，下标r之后的元素不会被删除
                w += size - r;
            }
            if (w != size) {
                // clear to let GC do its work
                //删除下标w之后的元素
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                //修改实际元素个数
                size = w;
                modified = true;
            }
        }
        return modified;
    }

    //私有方法，将ArrayList实例序列化
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        // Write out element count, and any hidden stuff
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    //私有方法，从反序列化中重构ArrayList实例
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;

        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in capacity
        s.readInt(); // ignored

        if (size > 0) {
            // be like clone(), allocate array based upon size not capacity
            int capacity = calculateCapacity(elementData, size);
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // Read in all elements in the proper order.
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    //返回一个起始索引为index的列表迭代器
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index);
    }

    //返回一个起始索引为0的列表迭代器
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    //返回一个迭代器
    public Iterator<E> iterator() {
        return new Itr();
    }

    //迭代器内部类
    private class Itr implements Iterator<E> {
        //记录下一个元素的索引
        int cursor;       // index of next element to return
        //记录刚被读取且未被删除的元素坐标，若为-1，则证明刚被读取的元素被删除或者未开始迭代元素
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        Itr() {}

        //判断是否还有元素未被迭代器读取
        public boolean hasNext() {
            return cursor != size;
        }

        //获取下一个元素
        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            //如果下一个元素索引大于等于size，则抛出异常
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            //如果下一个元素坐标大于存储元素数组长度，也抛出异常
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            //下一个元素索引加一
            cursor = i + 1;
            //返回元素，且将lastRet更改为这个被读取的元素下标
            return (E) elementData[lastRet = i];
        }

        //删除刚被读取的元素
        public void remove() {
            //当记录刚被读取的元素的值小于0，即等于-1时，证明未开始读取元素，或者元素已被删除，所以抛出异常
            if (lastRet < 0)
                throw new IllegalStateException();
            //检查现在modCount是否还等于新建迭代器时的modCount
            checkForComodification();

            try {
                //调用ArrayList的remove方法删除元素
                ArrayList.this.remove(lastRet);
                //将记录下一个元素索引的变量值变为被删除元素的索引值
                //因为删除元素后，后面元素会往前移动一位
                cursor = lastRet;
                //将记录刚被读取元素索引的变量值变为-1，防止多次调用remove
                lastRet = -1;
                //更新迭代器中记录modCount域的值（因为执行删除操作后，ArrayList的modCount域会改变）
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        //对迭代器中剩下的未被迭代的元素进行迭代，并用传入规则对元素进行操作
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            //检查传入的规则是否为null
            Objects.requireNonNull(consumer);
            final int size = ArrayList.this.size;
            int i = cursor;
            //如果下一个元素下标已经大于或等于list中元素个数了，则直接返回
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            //当下标i小于size，且无其他线程修改list中元素时，
            //不断读取元素，且用accept方法操作每个读取的元素
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // update once at end of iteration to reduce heap write traffic
            //将下一个元素下标记录为最后一个元素下标+1，即已经将所有元素迭代完毕
            cursor = i;
            //将记录刚被读取的元素索引值更改为最后一个元素下标
            lastRet = i - 1;
            checkForComodification();
        }

        //fast-fail机制，防止在迭代过程中，有其他线程修改ArrayList元素
        //只要list中修改次数与迭代器中记录的修改次数不一样，则抛出异常
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    //列表迭代器内部类，列表迭代器操作比迭代器操作更丰富，且可以指定从某个元素开始迭代
    private class ListItr extends Itr implements ListIterator<E> {
        //指定从index索引位置开始迭代元素
        ListItr(int index) {
            super();
            cursor = index;
        }

        //检查是否还有前一个元素
        public boolean hasPrevious() {
            return cursor != 0;
        }

        //返回下一个要迭代的元素下标
        public int nextIndex() {
            return cursor;
        }

        //返回前一个元素下标
        public int previousIndex() {
            return cursor - 1;
        }

        //返回前一个元素
        @SuppressWarnings("unchecked")
        public E previous() {
            //fast-fail机制
            checkForComodification();
            //前一个元素下标
            int i = cursor - 1;
            //若下标小于0，抛出异常
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            //将下一个要读取的元素的下标更新为前一个元素，使其重新开始从前一个元素开始迭代
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        //更新刚被读取的元素
        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        //在下一个元素位置添加新元素
        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                //在下一个元素位置添加新的元素
                ArrayList.this.add(i, e);
                //则下一个要读取的元素下标加一，所以迭代器无法读取到刚添加的值
                cursor = i + 1;
                //且将记录刚被读取元素的值改为-1，防止remove和set方法调用
                lastRet = -1;
                //更新迭代器内记录list修改次数的值
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    //返回下标从fromIndex开始，到toIndex的子列表
    public List<E> subList(int fromIndex, int toIndex) {
        //检查传入坐标是否越界或者小于0
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }

    ////检查传入坐标是否越界或者小于0
    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
    }

    private class SubList extends AbstractList<E> implements RandomAccess {
        //保存ArrayList自身
        private final AbstractList<E> parent;
        //记录子列表在父列表中的偏移量，其实就是起始下标
        private final int parentOffset;
        //记录子列表的偏移量
        private final int offset;
        //记录子列表的大小
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = ArrayList.this.modCount;
        }

        //更新传入指定下标的元素值，实际上是操作了父列表
        public E set(int index, E e) {
            //检查传入下标是否越界或者小于0
            rangeCheck(index);
            //fast-fail机制
            checkForComodification();
            E oldValue = ArrayList.this.elementData(offset + index);
            //修改父列表中在偏移量基础的index下标下的值
            ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }

        //获取父列表在偏移量下index下标值
        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return ArrayList.this.elementData(offset + index);
        }

        //获取父列表的元素个数
        public int size() {
            checkForComodification();
            return this.size;
        }

        //在指定坐标新增元素
        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = parent.modCount;
            this.size++;
        }

        //删除指定下标元素
        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }

        //删除指定下标范围元素
        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                    parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }

        //将传入集合中所有元素都添加到父列表所以元素后面
        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        //将集合中所有元素都添加到指定索引位置后面
        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }

        //返回一个列表迭代器
        public Iterator<E> iterator() {
            return listIterator();
        }

        //返回一个起始位置从index开始的列表迭代器
        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = ArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // update once at end of iteration to reduce heap write traffic
                    lastRet = cursor = i;
                    checkForComodification();
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        //返回一个新的子列表，子列表的不断分割
        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex);
        }

        //检查读取值或者删除值或者修改值时传入坐标是否越界或者小于0
        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        //检查添加值时传入坐标是否越界或者小于0
        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        //返回异常信息
        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }

        //fast-fail机制
        private void checkForComodification() {
            if (ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        //返回一个子列表的分割器
        public Spliterator<E> spliterator() {
            checkForComodification();
            return new ArrayListSpliterator<E>(ArrayList.this, offset,
                    offset + this.size, this.modCount);
        }
    }

    //遍历ArrayList中所有元素，并对每个元素进行传入规则处理
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    //返回一个包含全部元素的分割器实例
    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }

    /** Index-based split-by-two, lazily initialized Spliterator */
    //基于索引的，二分分割的，懒加载的分割器
    static final class ArrayListSpliterator<E> implements Spliterator<E> {



        private final ArrayList<E> list;
        //分割器起始索引（包含）
        private int index; // current index, modified on advance/split
        //分割器末尾索引（不包含），-1表示到最后一个元素
        private int fence; // -1 until used; then one past last index
        //fast-fail机制，用来判断外部是否在迭代过程中修改了元素
        private int expectedModCount; // initialized when fence set

        /** Create new spliterator covering the given  range */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        //获取实际末尾索引
        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            ArrayList<E> lst;
            //如果fence小于0，则证明为-1，表示到最后一个元素
            if ((hi = fence) < 0) {
                //如果fence小于0且list为null，则末尾索引为0
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    //如果fence小于0且list不为null，则设置末尾索引为list的元素个数
                    expectedModCount = lst.modCount;
                    hi = fence = lst.size;
                }
            }
            //如果fence大于0，则证明fence就是实际末尾索引
            return hi;
        }

        //获取新的分割器，二分分割
        public ArrayListSpliterator<E> trySplit() {
            //hi为末尾索引，lo为起始索引，mid为中间索引
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            //如果起始索引lo大于中间索引mid，则返回起始索引为lo，末尾索引为mid的新分割器
            return (lo >= mid) ? null : // divide range in half unless too small
                    new ArrayListSpliterator<E>(list, lo, index = mid,
                            expectedModCount);
        }

        //对当前下标元素进行传入规则处理
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                //下标前移
                index = i + 1;
                //取出改下标元素
                @SuppressWarnings("unchecked") E e = (E)list.elementData[i];
                //元素应用传入的规则
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                //返回true证明可能还有元素未处理
                return true;
            }
            //返回false表示分割器中元素已经都被处理过了
            return false;
        }

        //对分割器中剩下的元素全部遍历操作，并对元素进行传入规则处理
        public void forEachRemaining(Consumer<? super E> action) {
            //i记录下标，hi为分割器最后一个元素下标，mc记录modCount域
            int i, hi, mc; // hoist accesses and checks from loop
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                }
                else
                    mc = expectedModCount;
                //这里让index等于hi表示已将分割器中所有元素处理完
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }

        //返回分割器中未处理元素个数
        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    //删除ArrayList中满足传入规则的元素
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // figure out which elements are to be removed
        // any exception thrown from the filter predicate at this stage
        // will leave the collection unmodified
        //记录需要删除元素的个数
        int removeCount = 0;
        //用来放置满足条件元素的下标
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            //测试元素是否符合传入规则，若是则将bitSet中下标为i的数设置为true
            //同时删除数目加一;bitSet中每一位默认值都为false（0）
            if (filter.test(element)) {
                removeSet.set(i);
                //记录删除元素个数的变量值加一
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // shift surviving elements left over the spaces left by removed elements
        //用来判断是否有要删除的元素
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            //删除元素后，剩余的元素个数
            final int newSize = size - removeCount;
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                //nextClearBit方法：若该下标本身值为false，则返回传入自身下标，
                //若不为false，则继续往下寻找，直到有一个值为false，返回该值下标
                //这个方法可以获取到不需要删除元素的下标
                i = removeSet.nextClearBit(i);
                //将要保留下来的元素不断往前覆盖，达到删除的效果
                elementData[j] = elementData[i];
            }
            //对空元素进行赋值null处理
            for (int k=newSize; k < size; k++) {
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    //按照传入的规则将ArrayList中的元素进行替换
    /*
        UnaryOperator接口实例，UnaryOperator接口继承Function接口，
        Function<T,R>接口中定义了一个方法apply，接受T类型参数，返回R类型参数；
        这个接口，只接收一个泛型参数T，集成Function接口，
        也就是说，传入泛型T类型的参数，调用apply后，返回也T类型的参数；
        这个接口定义了一个静态方法，返回泛型对象的本身；
        示例：
        UnaryOperator<Integer> uo= x -> x + 1;
        System.out.println(uo.apply(10));// 11
        UnaryOperator<String> uo= x -> x + "asd";
        System.out.println(uo.apply("aa"));// aaasd
    */
    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        //检验传入参数是否为null，是则抛出异常
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        //遍历数组，用apply方法将数组元素按照operator定义规则进行替换
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    //根据传入的Comparator比较器对元素进行排序
    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}
