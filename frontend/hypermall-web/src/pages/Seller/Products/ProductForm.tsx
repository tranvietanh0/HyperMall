import { useEffect, useMemo, useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import toast from 'react-hot-toast';
import { useNavigate, useParams } from 'react-router-dom';
import Button from '@/components/common/Button';
import Input from '@/components/common/Input';
import Loading from '@/components/common/Loading';
import { productService, sellerProductService } from '@/services';
import type { SellerProductFormValues } from '@/types';
import { getErrorMessage } from '@/utils';

const createSlug = (value: string) => value
  .trim()
  .toLowerCase()
  .replace(/[^a-z0-9]+/g, '-')
  .replace(/^-+|-+$/g, '')
  .slice(0, 300);

const productSchema = Yup.object({
  categoryId: Yup.number().required('Please enter a category ID').min(1, 'Category ID must be greater than 0'),
  brandId: Yup.number().nullable().transform((value, originalValue) => (originalValue === '' ? null : value)),
  name: Yup.string().trim().required('Please enter a product name'),
  slug: Yup.string().trim().required('Please enter a slug'),
  description: Yup.string().max(5000, 'Description is too long'),
  shortDescription: Yup.string().max(500, 'Short description is too long'),
  thumbnail: Yup.string().url('Thumbnail must be a valid URL').required('Please enter a thumbnail URL'),
  basePrice: Yup.number().required('Please enter base price').min(0, 'Base price must be at least 0'),
  salePrice: Yup.number().nullable().transform((value, originalValue) => (originalValue === '' ? null : value)).min(0, 'Sale price must be at least 0'),
  status: Yup.mixed<'DRAFT' | 'PENDING' | 'ACTIVE' | 'INACTIVE'>().oneOf(['DRAFT', 'PENDING', 'ACTIVE', 'INACTIVE']).optional(),
  hasVariants: Yup.boolean().optional(),
});

export default function SellerProductFormPage() {
  const navigate = useNavigate();
  const { id } = useParams<{ id?: string }>();
  const isEditMode = Boolean(id);
  const [loading, setLoading] = useState(isEditMode);
  const [submitting, setSubmitting] = useState(false);

  const formik = useFormik<SellerProductFormValues>({
    enableReinitialize: true,
    initialValues: {
      categoryId: 1,
      brandId: undefined,
      name: '',
      slug: '',
      description: '',
      shortDescription: '',
      thumbnail: '',
      basePrice: 0,
      salePrice: undefined,
      status: 'DRAFT',
      hasVariants: false,
      images: [],
      variants: [],
    },
    validationSchema: productSchema,
    onSubmit: async (values) => {
      setSubmitting(true);
      try {
        const normalizedImages = values.images
          ?.filter((image) => image.url.trim().length > 0)
          .map((image, index) => ({
            ...image,
            url: image.url.trim(),
            sortOrder: image.sortOrder ?? index,
            isMain: image.isMain ?? index === 0,
          }));

        const normalizedVariants = values.variants
          ?.filter((variant) => variant.sku.trim().length > 0 || variant.name.trim().length > 0)
          .map((variant) => ({
            ...variant,
            sku: variant.sku.trim(),
            name: variant.name.trim(),
            image: variant.image?.trim() || undefined,
            salePrice: variant.salePrice || undefined,
            isActive: variant.isActive ?? true,
          }));

        const payload: SellerProductFormValues = {
          ...values,
          name: values.name.trim(),
          slug: values.slug.trim().toLowerCase(),
          thumbnail: values.thumbnail.trim(),
          brandId: values.brandId || undefined,
          description: values.description?.trim() || undefined,
          shortDescription: values.shortDescription?.trim() || undefined,
          salePrice: values.salePrice || undefined,
          hasVariants: Boolean(normalizedVariants?.length),
          images: normalizedImages?.length ? normalizedImages : undefined,
          variants: normalizedVariants?.length ? normalizedVariants : undefined,
        };

        if (isEditMode && id) {
          await sellerProductService.updateProduct(Number(id), payload);
          toast.success('Product updated successfully');
        } else {
          await sellerProductService.createProduct(payload);
          toast.success('Product created successfully');
        }

        navigate('/seller/products');
      } catch (error: unknown) {
        toast.error(getErrorMessage(error, 'Unable to save product. Slug must be at least 3 characters and use lowercase letters, numbers, or hyphens.'));
      } finally {
        setSubmitting(false);
      }
    },
  });

  useEffect(() => {
    if (!isEditMode || !id) {
      return;
    }

    const loadProduct = async () => {
      setLoading(true);
      try {
        const product = await productService.getProductById(id);
        formik.setValues({
          categoryId: product.category.id,
          brandId: product.brand?.id,
          name: product.name,
          slug: product.slug,
          description: product.description || '',
          shortDescription: product.shortDescription || '',
          thumbnail: product.thumbnail,
          basePrice: product.basePrice,
          salePrice: product.salePrice,
          status: product.status,
          hasVariants: product.hasVariants,
          images: product.images.map((image) => ({
            url: image.url,
            sortOrder: image.sortOrder,
            isMain: image.isMain,
          })),
          variants: product.variants.map((variant) => ({
            sku: variant.sku,
            name: variant.name,
            price: variant.price,
            salePrice: variant.salePrice,
            image: variant.image,
            attributes: variant.attributes,
            stock: variant.stock,
            isActive: variant.isActive,
          })),
        });
      } catch (error: unknown) {
        toast.error(getErrorMessage(error, 'Unable to load product details'));
      } finally {
        setLoading(false);
      }
    };

    void loadProduct();
  }, [formik, id, isEditMode]);

  const imageRows = useMemo(() => formik.values.images ?? [], [formik.values.images]);
  const variantRows = useMemo(() => formik.values.variants ?? [], [formik.values.variants]);

  if (loading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center">
        <Loading size="lg" text="Loading product form..." />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <h1 className="text-2xl font-semibold text-gray-900">{isEditMode ? 'Edit product' : 'Create product'}</h1>
        <p className="mt-1 text-sm text-gray-500">Use this form to manage your seller catalog.</p>
      </div>

      <form onSubmit={formik.handleSubmit} className="space-y-6 rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
        <section className="space-y-4">
          <h2 className="text-lg font-semibold text-gray-900">Basic information</h2>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <Input
              label="Product name"
              {...formik.getFieldProps('name')}
              onChange={(event) => {
                formik.handleChange(event);
                if (!isEditMode || !formik.values.slug) {
                  formik.setFieldValue('slug', createSlug(event.target.value));
                }
              }}
              error={formik.touched.name ? formik.errors.name : undefined}
            />
            <Input
              label="Slug"
              {...formik.getFieldProps('slug')}
              onChange={(event) => {
                formik.setFieldValue('slug', createSlug(event.target.value));
              }}
              error={formik.touched.slug ? formik.errors.slug : undefined}
            />
            <Input
              label="Category ID"
              type="number"
              {...formik.getFieldProps('categoryId')}
              error={formik.touched.categoryId ? String(formik.errors.categoryId ?? '') : undefined}
            />
            <Input
              label="Brand ID"
              type="number"
              value={formik.values.brandId ?? ''}
              onChange={formik.handleChange}
              name="brandId"
              error={formik.touched.brandId ? String(formik.errors.brandId ?? '') : undefined}
            />
            <Input
              label="Base price"
              type="number"
              {...formik.getFieldProps('basePrice')}
              error={formik.touched.basePrice ? String(formik.errors.basePrice ?? '') : undefined}
            />
            <Input
              label="Sale price"
              type="number"
              value={formik.values.salePrice ?? ''}
              onChange={formik.handleChange}
              name="salePrice"
              error={formik.touched.salePrice ? String(formik.errors.salePrice ?? '') : undefined}
            />
            <Input
              label="Thumbnail URL"
              {...formik.getFieldProps('thumbnail')}
              error={formik.touched.thumbnail ? formik.errors.thumbnail : undefined}
            />
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Status</label>
              <select
                name="status"
                value={formik.values.status ?? 'DRAFT'}
                onChange={formik.handleChange}
                className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <option value="DRAFT">DRAFT</option>
                <option value="PENDING">PENDING</option>
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </div>
          </div>
          <div>
            <label htmlFor="shortDescription" className="mb-1 block text-sm font-medium text-gray-700">
              Short description
            </label>
            <textarea
              id="shortDescription"
              rows={2}
              className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
              {...formik.getFieldProps('shortDescription')}
            />
          </div>
          <div>
            <label htmlFor="description" className="mb-1 block text-sm font-medium text-gray-700">
              Description
            </label>
            <textarea
              id="description"
              rows={5}
              className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-primary-500"
              {...formik.getFieldProps('description')}
            />
          </div>
        </section>

        <section className="space-y-4 border-t border-gray-100 pt-6">
          <div className="flex items-center justify-between gap-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">Images</h2>
              <p className="text-sm text-gray-500">Optional image rows for the product gallery.</p>
            </div>
            <Button
              type="button"
              variant="outline"
              onClick={() => formik.setFieldValue('images', [...imageRows, { url: '', sortOrder: imageRows.length, isMain: imageRows.length === 0 }])}
            >
              Add image
            </Button>
          </div>
          <div className="space-y-3">
            {imageRows.length === 0 ? <p className="text-sm text-gray-500">No extra images added.</p> : null}
            {imageRows.map((image, index) => (
              <div key={`image-${index}`} className="grid grid-cols-1 gap-3 rounded-xl border border-gray-200 p-4 md:grid-cols-[1fr_140px_120px_auto]">
                <Input
                  label="Image URL"
                  value={image.url}
                  onChange={(event) => {
                    const next = [...imageRows];
                    next[index] = { ...next[index], url: event.target.value };
                    formik.setFieldValue('images', next);
                  }}
                />
                <Input
                  label="Sort order"
                  type="number"
                  value={image.sortOrder ?? index}
                  onChange={(event) => {
                    const next = [...imageRows];
                    next[index] = { ...next[index], sortOrder: Number(event.target.value) };
                    formik.setFieldValue('images', next);
                  }}
                />
                <label className="flex items-center gap-2 pt-8 text-sm text-gray-700">
                  <input
                    type="checkbox"
                    checked={Boolean(image.isMain)}
                    onChange={(event) => {
                      const next = [...imageRows].map((item, itemIndex) => ({
                        ...item,
                        isMain: itemIndex === index ? event.target.checked : false,
                      }));
                      formik.setFieldValue('images', next);
                    }}
                  />
                  Main image
                </label>
                <div className="flex items-end">
                  <Button
                    type="button"
                    variant="ghost"
                    className="text-red-600 hover:bg-red-50 hover:text-red-700"
                    onClick={() => formik.setFieldValue('images', imageRows.filter((_, imageIndex) => imageIndex !== index))}
                  >
                    Remove
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="space-y-4 border-t border-gray-100 pt-6">
          <div className="flex items-center justify-between gap-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">Variants</h2>
              <p className="text-sm text-gray-500">Optional variant rows if the product has multiple options.</p>
            </div>
            <Button
              type="button"
              variant="outline"
              onClick={() => {
                formik.setFieldValue('hasVariants', true);
                formik.setFieldValue('variants', [
                  ...variantRows,
                  { sku: '', name: '', price: 0, stock: 0, attributes: {}, isActive: true },
                ]);
              }}
            >
              Add variant
            </Button>
          </div>
          <div className="space-y-3">
            {variantRows.length === 0 ? <p className="text-sm text-gray-500">No variants added.</p> : null}
            {variantRows.map((variant, index) => (
              <div key={`variant-${index}`} className="grid grid-cols-1 gap-3 rounded-xl border border-gray-200 p-4 md:grid-cols-2 xl:grid-cols-4">
                <Input
                  label="Variant name"
                  value={variant.name}
                  onChange={(event) => {
                    const next = [...variantRows];
                    next[index] = { ...next[index], name: event.target.value };
                    formik.setFieldValue('variants', next);
                  }}
                />
                <Input
                  label="SKU"
                  value={variant.sku}
                  onChange={(event) => {
                    const next = [...variantRows];
                    next[index] = { ...next[index], sku: event.target.value };
                    formik.setFieldValue('variants', next);
                  }}
                />
                <Input
                  label="Price"
                  type="number"
                  value={variant.price}
                  onChange={(event) => {
                    const next = [...variantRows];
                    next[index] = { ...next[index], price: Number(event.target.value) };
                    formik.setFieldValue('variants', next);
                  }}
                />
                <Input
                  label="Stock"
                  type="number"
                  value={variant.stock}
                  onChange={(event) => {
                    const next = [...variantRows];
                    next[index] = { ...next[index], stock: Number(event.target.value) };
                    formik.setFieldValue('variants', next);
                  }}
                />
                <div className="md:col-span-2 xl:col-span-4 flex justify-end">
                  <Button
                    type="button"
                    variant="ghost"
                    className="text-red-600 hover:bg-red-50 hover:text-red-700"
                    onClick={() => formik.setFieldValue('variants', variantRows.filter((_, variantIndex) => variantIndex !== index))}
                  >
                    Remove variant
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </section>

        <div className="flex flex-col-reverse gap-3 border-t border-gray-100 pt-6 sm:flex-row sm:justify-end">
          <Button type="button" variant="ghost" onClick={() => navigate('/seller/products')}>
            Cancel
          </Button>
          <Button type="submit" isLoading={submitting}>
            {isEditMode ? 'Save changes' : 'Create product'}
          </Button>
        </div>
      </form>
    </div>
  );
}
