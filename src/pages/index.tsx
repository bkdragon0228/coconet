import React, { useState, useCallback, useMemo, useEffect } from 'react'
import { useArticleService } from '../api/services/articleService'
import { Article } from '../models/article'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { roleOptions } from '../utils/utils'
import { useAppDispatch, useAppSelector } from '../store/RootReducer'
import Pagination from '@mui/material/Pagination'
import CircularProgress from '@mui/material/CircularProgress'
import MultiStackSelector from '../components/organisms/multiStackSelector/multiStackSelector'
import CustomCarousel from '../components/organisms/Carousel'
import styled from 'styled-components'
import Tab from '../components/organisms/customTabs/Tab'
import Tabs from '../components/organisms/customTabs/Tabs'
import Card from '../components/molecules/card/card'
import FilterSelect from '../components/atoms/Select/filterSelect'
import AdditionalModal from '../components/organisms/modal/additionalModal'
import { setToken } from '../store/authSlice'
import PopularArticleList from '../components/organisms/domain/popularArticleList'

const Index = () => {
  const token = useAppSelector((state) => state.reducer.auth.token)
  const dispatch = useAppDispatch()
  const articleService = useArticleService()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const memberId = useMemo(
    () => searchParams.get('memberId') || '',
    [searchParams.get('memberId')],
  )
  const accessToken = useMemo(
    () => searchParams.get('accessToken') || '',
    [searchParams.get('accessToken')],
  )
  const [isLoading, setIsLoading] = useState<boolean>(false)
  const [articleList, setArticleList] = useState<Article[]>([])
  const [page, setPage] = useState(1)
  const [pageData, setPageData] = useState<{
    totalElements: number
    totalPages: number
  }>({
    totalElements: 0,
    totalPages: 0,
  })

  // 필터링 관련 state
  const [selectedStacks, setSelectedStacks] = useState<string[]>([])
  const [selectedPosition, setSelectedPosition] = useState<string>('')

  const handleChange = (event: any, value: number) => {
    setPage(value)

    // TODO 데이터 요청
  }

  const fetchAllArticle = async (arg: {
    roles?: string[]
    stacks?: string[]
    bookmark?: boolean
  }) => {
    setIsLoading(true)

    try {
      const result = await articleService.getAllArticle(arg)

      setPageData({
        totalElements: result.totalElements,
        totalPages: result.totalPages,
      })

      if (result.data) {
        setArticleList(result.data)
      }
    } catch (error) {
      console.log(error)
      setArticleList([])
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchAllArticle({})
  }, [token])

  /**
   * 기술을 선택하는 함수
   */
  const hanldeSelected = useCallback(
    (value: string) => {
      if (checkFor()) {
        const filteredSelected = selectedStacks.filter(
          (skill) => skill !== value,
        )
        fetchAllArticle({ stacks: filteredSelected, roles: [selectedPosition] })
        setSelectedStacks(filteredSelected)

        return
      }

      if (!checkFor()) {
        fetchAllArticle({
          stacks: [...selectedStacks, value],
          roles: [selectedPosition],
        })

        setSelectedStacks((prev) => [...prev, value])

        return
      }

      function checkFor() {
        return selectedStacks.indexOf(value) >= 0
      }
    },
    [selectedStacks],
  )

  /**
   * 포지션을 선택하는 함수
   */
  const handlePosiionSelected = useCallback((value: string) => {
    fetchAllArticle({ roles: [value], stacks: selectedStacks })
    setSelectedPosition(value)
  }, [])

  useEffect(() => {
    if (memberId) {
      document.body.style.overflow = 'hidden'
      localStorage.setItem('memberUUID', memberId)
    } else {
      document.body.style.overflow = 'unset'
    }
  }, [memberId])

  useEffect(() => {
    if (accessToken) {
      localStorage.setItem('accessToken', accessToken)
      dispatch(setToken({ token: accessToken }))
    }
  }, [accessToken])

  return (
    <>
      <div style={{ paddingTop: 125 }}>
        <StyledContents>
          <CustomCarousel />
          {/* 인기글 추기 */}
          <PopularArticleList />
          <div style={{ display: 'flex', columnGap: '1.5rem' }}>
            <MultiStackSelector
              handleSelected={hanldeSelected}
              selected={selectedStacks}
            />
            <FilterSelect
              title="포지션"
              selected={selectedPosition}
              handleSelected={handlePosiionSelected}
              options={roleOptions}
            />
          </div>

          <Tabs
            options={[
              { name: '전체' },
              { name: '프로젝트' },
              { name: '스터디' },
            ]}
          >
            <Tab value="전체">
              {isLoading && (
                <StyledLoadingWrapper>
                  <CircularProgress />
                </StyledLoadingWrapper>
              )}
              {!isLoading && (
                <StyledItemWrpper>
                  {articleList?.map((article) => <Card item={article} />)}
                </StyledItemWrpper>
              )}
            </Tab>
            <Tab value="프로젝트">
              {isLoading && (
                <StyledLoadingWrapper>
                  <CircularProgress />
                </StyledLoadingWrapper>
              )}
              {!isLoading && (
                <StyledItemWrpper>
                  {articleList
                    ?.filter((article) => article.articleType === 'PROJECT')
                    .map((article) => <Card item={article} />)}
                </StyledItemWrpper>
              )}
            </Tab>
            <Tab value="스터디">
              {isLoading && (
                <StyledLoadingWrapper>
                  <CircularProgress />
                </StyledLoadingWrapper>
              )}
              {!isLoading && (
                <>
                  {articleList.length > 0 ? (
                    <StyledItemWrpper>
                      {articleList
                        ?.filter((article) => article.articleType === 'STUDY')
                        .map((article) => <Card item={article} />)}
                    </StyledItemWrpper>
                  ) : (
                    <StyledItemWrpper>
                      <div>원하시는 게시글이 없습니다.</div>
                    </StyledItemWrpper>
                  )}
                </>
              )}
            </Tab>
          </Tabs>
          <div style={{ width: '100%', margin: '0 auto' }}>
            <Pagination
              count={pageData.totalPages}
              page={page}
              onChange={handleChange} // TODO : 늘어나는 페이지에 따라 추가 요청
              variant="outlined"
              shape="rounded"
            />
          </div>
        </StyledContents>
      </div>
      <AdditionalModal
        open={memberId ? true : false}
        handleClose={() => navigate('/')}
        memberId={memberId}
      />
    </>
  )
}

export default Index

const StyledContents = styled.div`
  width: 1220px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 2rem;
`

const StyledItemWrpper = styled.div`
  margin-top: 1rem;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
`

const StyledLoadingWrapper = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 50vh;
`
