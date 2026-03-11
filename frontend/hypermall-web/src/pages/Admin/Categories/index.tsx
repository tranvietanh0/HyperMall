import { useState, useEffect } from 'react';
import {
  PlusIcon,
  PencilIcon,
  TrashIcon,
  ChevronRightIcon,
} from '@heroicons/react/24/outline';
import clsx from 'clsx';

interface Category {
  id: number;
  name: string;
  slug: string;
  parentId: number | null;
  level: number;
  productCount: number;
  isActive: boolean;
  children?: Category[];
}

export default function AdminCategories() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());
  const [showAddModal, setShowAddModal] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');
  const [selectedParentId, setSelectedParentId] = useState<number | null>(null);

  useEffect(() => {
    setTimeout(() => {
      setCategories([
        {
          id: 1, name: 'Dien tu', slug: 'dien-tu', parentId: null, level: 0, productCount: 500, isActive: true,
          children: [
            { id: 11, name: 'Dien thoai', slug: 'dien-thoai', parentId: 1, level: 1, productCount: 200, isActive: true },
            { id: 12, name: 'Laptop', slug: 'laptop', parentId: 1, level: 1, productCount: 150, isActive: true },
            { id: 13, name: 'Tablet', slug: 'tablet', parentId: 1, level: 1, productCount: 100, isActive: true },
            { id: 14, name: 'Phu kien', slug: 'phu-kien', parentId: 1, level: 1, productCount: 50, isActive: true },
          ]
        },
        {
          id: 2, name: 'Thoi trang', slug: 'thoi-trang', parentId: null, level: 0, productCount: 800, isActive: true,
          children: [
            { id: 21, name: 'Ao', slug: 'ao', parentId: 2, level: 1, productCount: 300, isActive: true },
            { id: 22, name: 'Quan', slug: 'quan', parentId: 2, level: 1, productCount: 250, isActive: true },
            { id: 23, name: 'Giay dep', slug: 'giay-dep', parentId: 2, level: 1, productCount: 200, isActive: true },
            { id: 24, name: 'Tui xach', slug: 'tui-xach', parentId: 2, level: 1, productCount: 50, isActive: false },
          ]
        },
        {
          id: 3, name: 'Nha cua', slug: 'nha-cua', parentId: null, level: 0, productCount: 300, isActive: true,
          children: [
            { id: 31, name: 'Noi that', slug: 'noi-that', parentId: 3, level: 1, productCount: 150, isActive: true },
            { id: 32, name: 'Trang tri', slug: 'trang-tri', parentId: 3, level: 1, productCount: 100, isActive: true },
            { id: 33, name: 'Dung cu', slug: 'dung-cu', parentId: 3, level: 1, productCount: 50, isActive: true },
          ]
        },
      ]);
      setLoading(false);
    }, 500);
  }, []);

  const toggleExpand = (id: number) => {
    const newExpanded = new Set(expandedIds);
    if (newExpanded.has(id)) {
      newExpanded.delete(id);
    } else {
      newExpanded.add(id);
    }
    setExpandedIds(newExpanded);
  };

  const handleAddCategory = () => {
    if (!newCategoryName.trim()) return;

    const newCategory: Category = {
      id: Date.now(),
      name: newCategoryName,
      slug: newCategoryName.toLowerCase().replace(/\s+/g, '-'),
      parentId: selectedParentId,
      level: selectedParentId ? 1 : 0,
      productCount: 0,
      isActive: true,
    };

    if (selectedParentId) {
      setCategories(categories.map(cat => {
        if (cat.id === selectedParentId) {
          return {
            ...cat,
            children: [...(cat.children || []), newCategory],
          };
        }
        return cat;
      }));
    } else {
      setCategories([...categories, { ...newCategory, children: [] }]);
    }

    setShowAddModal(false);
    setNewCategoryName('');
    setSelectedParentId(null);
  };

  const toggleStatus = (id: number, parentId: number | null) => {
    if (parentId) {
      setCategories(categories.map(cat => {
        if (cat.id === parentId) {
          return {
            ...cat,
            children: cat.children?.map(child =>
              child.id === id ? { ...child, isActive: !child.isActive } : child
            ),
          };
        }
        return cat;
      }));
    } else {
      setCategories(categories.map(cat =>
        cat.id === id ? { ...cat, isActive: !cat.isActive } : cat
      ));
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-4 border-blue-600 rounded-full animate-spin border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Category Management</h1>
        <button
          onClick={() => setShowAddModal(true)}
          className="flex items-center px-4 py-2 text-white bg-blue-600 rounded-lg hover:bg-blue-700"
        >
          <PlusIcon className="w-5 h-5 mr-2" />
          Add Category
        </button>
      </div>

      {/* Categories Tree */}
      <div className="bg-white rounded-lg shadow">
        <div className="p-4 border-b border-gray-200">
          <div className="grid grid-cols-12 text-xs font-medium tracking-wider text-gray-500 uppercase">
            <div className="col-span-5">Name</div>
            <div className="col-span-2">Slug</div>
            <div className="col-span-2">Products</div>
            <div className="col-span-1">Status</div>
            <div className="col-span-2">Actions</div>
          </div>
        </div>
        <div className="divide-y divide-gray-200">
          {categories.map((category) => (
            <div key={category.id}>
              {/* Parent Category */}
              <div className="grid items-center grid-cols-12 px-4 py-3 hover:bg-gray-50">
                <div className="flex items-center col-span-5">
                  {category.children && category.children.length > 0 && (
                    <button
                      onClick={() => toggleExpand(category.id)}
                      className="mr-2"
                    >
                      <ChevronRightIcon
                        className={clsx(
                          'w-5 h-5 text-gray-400 transition-transform',
                          expandedIds.has(category.id) && 'rotate-90'
                        )}
                      />
                    </button>
                  )}
                  <span className="font-medium text-gray-900">{category.name}</span>
                </div>
                <div className="col-span-2 text-sm text-gray-500">{category.slug}</div>
                <div className="col-span-2 text-sm text-gray-900">{category.productCount}</div>
                <div className="col-span-1">
                  <button
                    onClick={() => toggleStatus(category.id, null)}
                    className={clsx(
                      'px-2 py-1 text-xs font-medium rounded-full',
                      category.isActive
                        ? 'bg-green-100 text-green-800'
                        : 'bg-gray-100 text-gray-800'
                    )}
                  >
                    {category.isActive ? 'Active' : 'Inactive'}
                  </button>
                </div>
                <div className="flex items-center col-span-2 space-x-2">
                  <button className="p-1 text-gray-400 hover:text-yellow-600">
                    <PencilIcon className="w-5 h-5" />
                  </button>
                  <button className="p-1 text-gray-400 hover:text-red-600">
                    <TrashIcon className="w-5 h-5" />
                  </button>
                </div>
              </div>

              {/* Child Categories */}
              {expandedIds.has(category.id) && category.children?.map((child) => (
                <div
                  key={child.id}
                  className="grid items-center grid-cols-12 px-4 py-3 bg-gray-50 hover:bg-gray-100"
                >
                  <div className="flex items-center col-span-5 pl-8">
                    <span className="text-gray-700">{child.name}</span>
                  </div>
                  <div className="col-span-2 text-sm text-gray-500">{child.slug}</div>
                  <div className="col-span-2 text-sm text-gray-900">{child.productCount}</div>
                  <div className="col-span-1">
                    <button
                      onClick={() => toggleStatus(child.id, category.id)}
                      className={clsx(
                        'px-2 py-1 text-xs font-medium rounded-full',
                        child.isActive
                          ? 'bg-green-100 text-green-800'
                          : 'bg-gray-100 text-gray-800'
                      )}
                    >
                      {child.isActive ? 'Active' : 'Inactive'}
                    </button>
                  </div>
                  <div className="flex items-center col-span-2 space-x-2">
                    <button className="p-1 text-gray-400 hover:text-yellow-600">
                      <PencilIcon className="w-5 h-5" />
                    </button>
                    <button className="p-1 text-gray-400 hover:text-red-600">
                      <TrashIcon className="w-5 h-5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ))}
        </div>
      </div>

      {/* Add Category Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="w-full max-w-md p-6 bg-white rounded-lg">
            <h2 className="mb-4 text-lg font-semibold">Add New Category</h2>
            <div className="space-y-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Category Name
                </label>
                <input
                  type="text"
                  value={newCategoryName}
                  onChange={(e) => setNewCategoryName(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter category name"
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Parent Category (optional)
                </label>
                <select
                  value={selectedParentId || ''}
                  onChange={(e) => setSelectedParentId(e.target.value ? Number(e.target.value) : null)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="">None (Top Level)</option>
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="flex justify-end mt-6 space-x-3">
              <button
                onClick={() => setShowAddModal(false)}
                className="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={handleAddCategory}
                className="px-4 py-2 text-white bg-blue-600 rounded-lg hover:bg-blue-700"
              >
                Add
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
