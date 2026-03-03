import { useState, useEffect } from 'react'
import { useFormik } from 'formik'
import * as Yup from 'yup'
import toast from 'react-hot-toast'
import { UserCircleIcon, MapPinIcon, LockClosedIcon, PlusIcon, TrashIcon, CheckBadgeIcon } from '@heroicons/react/24/outline'
import { useAppDispatch, useAppSelector } from '@store/hooks'
import { setUser } from '@store/slices/authSlice'
import { api } from '@services/api.service'
import { API_ENDPOINTS } from '@config/api.config'
import { STORAGE_KEYS } from '@config/constants'
import Input from '@components/common/Input'
import Loading from '@components/common/Loading'
import type { Address, AddressRequest, ApiResponse, User } from '@/types'

type Tab = 'info' | 'addresses' | 'password'

export default function ProfilePage() {
  const [activeTab, setActiveTab] = useState<Tab>('info')

  const tabs = [
    { id: 'info' as Tab, label: 'Thông tin tài khoản', icon: UserCircleIcon },
    { id: 'addresses' as Tab, label: 'Địa chỉ', icon: MapPinIcon },
    { id: 'password' as Tab, label: 'Đổi mật khẩu', icon: LockClosedIcon },
  ]

  return (
    <div className="container mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold mb-6">Tài khoản của tôi</h1>
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* Sidebar */}
        <div className="md:col-span-1">
          <nav className="bg-white rounded-xl border overflow-hidden">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`w-full flex items-center gap-3 px-4 py-3 text-sm font-medium border-b last:border-0 transition-colors ${
                  activeTab === tab.id ? 'bg-primary-50 text-primary-700' : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                <tab.icon className="w-5 h-5" />
                {tab.label}
              </button>
            ))}
          </nav>
        </div>

        {/* Content */}
        <div className="md:col-span-3">
          {activeTab === 'info' && <InfoTab />}
          {activeTab === 'addresses' && <AddressesTab />}
          {activeTab === 'password' && <PasswordTab />}
        </div>
      </div>
    </div>
  )
}

function InfoTab() {
  const dispatch = useAppDispatch()
  const user = useAppSelector((s) => s.auth.user)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const formik = useFormik({
    enableReinitialize: true,
    initialValues: { fullName: user?.fullName ?? '', phone: user?.phone ?? '' },
    validationSchema: Yup.object({
      fullName: Yup.string().required('Vui lòng nhập họ tên'),
      phone: Yup.string().matches(/^(0|\+84)[3-9]\d{8}$/, 'Số điện thoại không hợp lệ'),
    }),
    onSubmit: async (values) => {
      setIsSubmitting(true)
      try {
        const res = await api.put<ApiResponse<User>>(API_ENDPOINTS.USERS.UPDATE_PROFILE, values)
        const updated = res.data
        dispatch(setUser(updated))
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(updated))
        toast.success('Cập nhật thông tin thành công')
      } catch (err: any) {
        toast.error(err?.response?.data?.message ?? 'Cập nhật thất bại')
      } finally {
        setIsSubmitting(false)
      }
    },
  })

  return (
    <div className="bg-white rounded-xl border p-6">
      <h2 className="font-semibold text-lg mb-5">Thông tin tài khoản</h2>

      <div className="flex items-center gap-4 mb-6 pb-6 border-b">
        <div className="w-16 h-16 rounded-full bg-primary-100 flex items-center justify-center text-primary-600 text-2xl font-bold flex-shrink-0">
          {user?.fullName?.charAt(0)?.toUpperCase() ?? '?'}
        </div>
        <div>
          <p className="font-semibold text-lg">{user?.fullName}</p>
          <p className="text-sm text-gray-500">{user?.email}</p>
          <div className="flex items-center gap-1 mt-1">
            {user?.emailVerified
              ? <><CheckBadgeIcon className="w-4 h-4 text-green-500" /><span className="text-xs text-green-600">Email đã xác thực</span></>
              : <span className="text-xs text-yellow-600">Email chưa xác thực</span>
            }
          </div>
        </div>
      </div>

      <form onSubmit={formik.handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input label="Họ và tên" {...formik.getFieldProps('fullName')} error={formik.touched.fullName ? formik.errors.fullName : undefined} />
          <Input label="Số điện thoại" {...formik.getFieldProps('phone')} error={formik.touched.phone ? formik.errors.phone : undefined} />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input value={user?.email ?? ''} disabled className="input w-full bg-gray-50 cursor-not-allowed" />
        </div>
        <div className="pt-2">
          <button type="submit" disabled={isSubmitting} className="btn btn-primary px-8">
            {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
          </button>
        </div>
      </form>
    </div>
  )
}

function AddressesTab() {
  const [addresses, setAddresses] = useState<Address[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)

  const fetchAddresses = () => {
    setIsLoading(true)
    api.get<ApiResponse<Address[]>>(API_ENDPOINTS.USERS.ADDRESSES)
      .then((res) => setAddresses(res.data ?? []))
      .catch(() => setAddresses([]))
      .finally(() => setIsLoading(false))
  }

  useEffect(() => { fetchAddresses() }, [])

  const handleDelete = async (id: number) => {
    if (!confirm('Xóa địa chỉ này?')) return
    try {
      await api.delete(`${API_ENDPOINTS.USERS.ADDRESSES}/${id}`)
      toast.success('Đã xóa địa chỉ')
      fetchAddresses()
    } catch {
      toast.error('Không thể xóa địa chỉ')
    }
  }

  const handleSetDefault = async (id: number) => {
    try {
      await api.put(`${API_ENDPOINTS.USERS.ADDRESSES}/${id}/default`)
      fetchAddresses()
    } catch {
      toast.error('Không thể cập nhật địa chỉ mặc định')
    }
  }

  if (isLoading) return <div className="flex justify-center py-16"><Loading /></div>

  return (
    <div className="space-y-4">
      <div className="bg-white rounded-xl border p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="font-semibold text-lg">Địa chỉ giao hàng</h2>
          <button onClick={() => { setEditingId(null); setShowForm(true) }}
            className="btn btn-primary text-sm flex items-center gap-1.5">
            <PlusIcon className="w-4 h-4" /> Thêm địa chỉ
          </button>
        </div>

        {addresses.length === 0 ? (
          <div className="text-center py-10 text-gray-400">
            <MapPinIcon className="w-10 h-10 mx-auto mb-2 opacity-40" />
            <p>Chưa có địa chỉ nào</p>
          </div>
        ) : (
          <div className="space-y-3">
            {addresses.map((addr) => (
              <div key={addr.id} className={`border rounded-lg p-4 ${addr.isDefault ? 'border-primary-400 bg-primary-50/40' : ''}`}>
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="font-medium text-sm">{addr.fullName}</span>
                      <span className="text-gray-400 text-sm">|</span>
                      <span className="text-sm text-gray-600">{addr.phone}</span>
                      {addr.isDefault && (
                        <span className="text-xs font-semibold text-primary-600 bg-primary-100 px-2 py-0.5 rounded-full">Mặc định</span>
                      )}
                      <span className="text-xs text-gray-500 bg-gray-100 px-2 py-0.5 rounded-full">
                        {addr.type === 'HOME' ? 'Nhà' : 'Văn phòng'}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600">{addr.addressDetail}, {addr.ward}, {addr.district}, {addr.province}</p>
                  </div>
                  <div className="flex items-center gap-2 flex-shrink-0">
                    <button onClick={() => { setEditingId(addr.id); setShowForm(true) }}
                      className="text-sm text-primary-600 hover:underline">Sửa</button>
                    <button onClick={() => handleDelete(addr.id)}
                      className="text-gray-400 hover:text-red-500 p-1">
                      <TrashIcon className="w-4 h-4" />
                    </button>
                  </div>
                </div>
                {!addr.isDefault && (
                  <button onClick={() => handleSetDefault(addr.id)}
                    className="mt-2 text-xs text-gray-500 hover:text-primary-600 border rounded px-2 py-0.5">
                    Đặt làm mặc định
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {showForm && (
        <AddressForm
          addressId={editingId}
          existingAddress={editingId ? addresses.find((a) => a.id === editingId) : undefined}
          onClose={() => setShowForm(false)}
          onSaved={() => { setShowForm(false); fetchAddresses() }}
        />
      )}
    </div>
  )
}

function AddressForm({
  addressId,
  existingAddress,
  onClose,
  onSaved,
}: {
  addressId: number | null
  existingAddress?: Address
  onClose: () => void
  onSaved: () => void
}) {
  const [isSubmitting, setIsSubmitting] = useState(false)

  const formik = useFormik<AddressRequest>({
    enableReinitialize: true,
    initialValues: {
      fullName: existingAddress?.fullName ?? '',
      phone: existingAddress?.phone ?? '',
      province: existingAddress?.province ?? '',
      district: existingAddress?.district ?? '',
      ward: existingAddress?.ward ?? '',
      addressDetail: existingAddress?.addressDetail ?? '',
      isDefault: existingAddress?.isDefault ?? false,
      type: existingAddress?.type ?? 'HOME',
    },
    validationSchema: Yup.object({
      fullName: Yup.string().required('Vui lòng nhập họ tên'),
      phone: Yup.string().matches(/^(0|\+84)[3-9]\d{8}$/, 'Số điện thoại không hợp lệ').required('Vui lòng nhập SĐT'),
      province: Yup.string().required('Vui lòng nhập tỉnh/thành phố'),
      district: Yup.string().required('Vui lòng nhập quận/huyện'),
      ward: Yup.string().required('Vui lòng nhập phường/xã'),
      addressDetail: Yup.string().required('Vui lòng nhập địa chỉ chi tiết'),
    }),
    onSubmit: async (values) => {
      setIsSubmitting(true)
      try {
        if (addressId) {
          await api.put(`${API_ENDPOINTS.USERS.ADDRESSES}/${addressId}`, values)
        } else {
          await api.post(API_ENDPOINTS.USERS.ADDRESSES, values)
        }
        toast.success(addressId ? 'Đã cập nhật địa chỉ' : 'Đã thêm địa chỉ mới')
        onSaved()
      } catch (err: any) {
        toast.error(err?.response?.data?.message ?? 'Không thể lưu địa chỉ')
      } finally {
        setIsSubmitting(false)
      }
    },
  })

  return (
    <div className="bg-white rounded-xl border p-6">
      <h3 className="font-semibold text-lg mb-4">{addressId ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới'}</h3>
      <form onSubmit={formik.handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input label="Họ và tên" {...formik.getFieldProps('fullName')} error={formik.touched.fullName ? formik.errors.fullName : undefined} />
          <Input label="Số điện thoại" {...formik.getFieldProps('phone')} error={formik.touched.phone ? formik.errors.phone : undefined} />
          <Input label="Tỉnh/Thành phố" {...formik.getFieldProps('province')} error={formik.touched.province ? formik.errors.province : undefined} />
          <Input label="Quận/Huyện" {...formik.getFieldProps('district')} error={formik.touched.district ? formik.errors.district : undefined} />
          <Input label="Phường/Xã" {...formik.getFieldProps('ward')} error={formik.touched.ward ? formik.errors.ward : undefined} />
          <Input label="Địa chỉ chi tiết" {...formik.getFieldProps('addressDetail')} error={formik.touched.addressDetail ? formik.errors.addressDetail : undefined} />
        </div>

        <div className="flex items-center gap-4 text-sm">
          <span className="font-medium text-gray-700">Loại địa chỉ:</span>
          {(['HOME', 'OFFICE'] as const).map((t) => (
            <label key={t} className="flex items-center gap-1.5 cursor-pointer">
              <input type="radio" name="type" value={t} checked={formik.values.type === t} onChange={() => formik.setFieldValue('type', t)} />
              {t === 'HOME' ? 'Nhà riêng' : 'Văn phòng'}
            </label>
          ))}
        </div>

        <label className="flex items-center gap-2 text-sm cursor-pointer">
          <input type="checkbox" {...formik.getFieldProps('isDefault')} checked={formik.values.isDefault} className="rounded" />
          Đặt làm địa chỉ mặc định
        </label>

        <div className="flex gap-3 pt-2">
          <button type="button" onClick={onClose} className="btn btn-outline px-6">Hủy</button>
          <button type="submit" disabled={isSubmitting} className="btn btn-primary px-6">
            {isSubmitting ? 'Đang lưu...' : 'Lưu địa chỉ'}
          </button>
        </div>
      </form>
    </div>
  )
}

function PasswordTab() {
  const [isSubmitting, setIsSubmitting] = useState(false)

  const formik = useFormik({
    initialValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
    validationSchema: Yup.object({
      currentPassword: Yup.string().required('Vui lòng nhập mật khẩu hiện tại'),
      newPassword: Yup.string().min(8, 'Mật khẩu ít nhất 8 ký tự').required('Vui lòng nhập mật khẩu mới'),
      confirmPassword: Yup.string()
        .oneOf([Yup.ref('newPassword')], 'Mật khẩu xác nhận không khớp')
        .required('Vui lòng xác nhận mật khẩu'),
    }),
    onSubmit: async (values, { resetForm }) => {
      setIsSubmitting(true)
      try {
        await api.put(API_ENDPOINTS.USERS.CHANGE_PASSWORD, {
          currentPassword: values.currentPassword,
          newPassword: values.newPassword,
        })
        toast.success('Đổi mật khẩu thành công')
        resetForm()
      } catch (err: any) {
        toast.error(err?.response?.data?.message ?? 'Đổi mật khẩu thất bại')
      } finally {
        setIsSubmitting(false)
      }
    },
  })

  return (
    <div className="bg-white rounded-xl border p-6">
      <h2 className="font-semibold text-lg mb-5">Đổi mật khẩu</h2>
      <form onSubmit={formik.handleSubmit} className="max-w-sm space-y-4">
        <Input label="Mật khẩu hiện tại" type="password" {...formik.getFieldProps('currentPassword')} error={formik.touched.currentPassword ? formik.errors.currentPassword : undefined} />
        <Input label="Mật khẩu mới" type="password" {...formik.getFieldProps('newPassword')} error={formik.touched.newPassword ? formik.errors.newPassword : undefined} />
        <Input label="Xác nhận mật khẩu mới" type="password" {...formik.getFieldProps('confirmPassword')} error={formik.touched.confirmPassword ? formik.errors.confirmPassword : undefined} />
        <div className="pt-2">
          <button type="submit" disabled={isSubmitting} className="btn btn-primary px-8">
            {isSubmitting ? 'Đang xử lý...' : 'Đổi mật khẩu'}
          </button>
        </div>
      </form>
    </div>
  )
}
