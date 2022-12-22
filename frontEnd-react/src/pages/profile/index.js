
import { ArrowCircleLeftOutline as Back, PencilAltOutline as Edit } from '@graywolfai/react-heroicons'

import { Shell, Link, Separator, Button } from '../../components'

import {useComments, useSearchUsers, useFriendship } from '../../hooks'

import {ListaComentsUser } from '../../components/comment'
import { useParams } from 'react-router-dom'

const backdrop = user => {
    const picture = user.picture === 'none' ? "https://media.istockphoto.com/id/517998264/es/vector/hombre-icono-de-usuario.jpg?s=612x612&w=is&k=20&c=qX2mEhSna0SxcxMXXyS8YZRaYBm8itZ9N9Ma1S4Bug4=" : user.picture

    return picture
}

export default function Profile() {
    const {id} = useParams()
    const {user} = useSearchUsers(id)
    
    return <Shell>
        {user.content ? <img style = {{ height: '28rem' }}
            src = { backdrop(user.content[0]) }
            alt = { `${user} backdrop` }
            className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' /> : null}

        <Link variant = 'primary'
            className = 'rounded-full absolute text-white top-4 left-8 flex items-center pl-2 pr-4 py-2 gap-4'
            to = '/'
        >
            <Back className = 'w-8 h-8'/>
            <span>Volver</span>
        </Link>
        
        {user.content ? <Links user = {user.content[0]}/> : null}

        {user.content ? <div className = 'mx-auto max-w-screen-2xl p-8'>
            <Header user = { user.content[0] } />
            <Comments user = { user.content[0] } />
        </div> : null}
    </Shell>
}

function Links({ user }){
    const {amistad, peticionAmistad} = useFriendship(localStorage.getItem('user'), user?.email)

    const handleClick = e => {
        //se envia una solicitud de amistad
        let today = new Date()
        let dd = String(today.getDate()).padStart(2, '0')
        let mm = String(today.getMonth() + 1).padStart(2, '0')
        let yyyy = today.getFullYear()

        let solicitud = {
            user: localStorage.getItem('user'),
            friend: user.email,
            since: {
                day: dd,
                month: mm,
                year: yyyy
            },
            confirmed: false
        }

        peticionAmistad(solicitud)
        alert('Solicitud de amistad enviada')
    }

    return <>{user.email  === localStorage.getItem('user') ? 
    <Link variant = 'primary'
        className = 'rounded-full absolute text-white top-4 right-8 flex items-center px-2 py-2 gap-4'
        to = {`/profile-edit/`}>
        <Edit className = 'w-8 h-8'/>
    </Link> : 
    amistad ? null : 
        <Button variant = 'primary'
            className = 'rounded-full absolute text-white top-8 right-8 flex items-center px-3 py-3 gap-4'
            onClick = {handleClick}>
            <span>AÑADIR AMIGO</span>
        </Button>}
    </>
}

function Header({ user }) {
    return <header className = 'mt-64 relative flex items-end pb-8 mb-8 '>
        {user.length === 0 ?  null : <><img style = {{ height: "250px" }}
             src = { backdrop(user) }
             alt = { `${ user?.email } poster` }
             className = 'w-64 rounded-full z-20' />
        <hgroup className = 'flex-1'>
            <h1 className = {`bg-black bg-opacity-50 backdrop-filter backdrop-blur 
                                          text-right text-white text-6xl font-bold
                                          p-8`}>
                { user.name }
            </h1>
            <div className='p-8 flex justify-center space-x-64'>
                <span className='flex'>
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-10 h-10">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5" />
                </svg>
                    <h1 className={`text-black text-4xl font-boldp-8`}> {user.birthday.day}/{user.birthday.month}/{user.birthday.year}  </h1>
                </span>

                <span className='flex'>
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-10 h-10">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1115 0z" />
                    </svg>

                    <h1 className={`text-black text-4xl font-boldp-8`}> {user.country}  </h1>
                </span>

                <span className='flex'>
                    <h1 className={`text-black text-4xl font-boldp-8`}> {user.email}  </h1>
                </span>
            </div>
        </hgroup> </>}
    </header>
}

function Comments({ user }) {
    const { comments} = useComments({ filter: { user : user.email } } )


    return <div className='mt-12 relative flex flex-col pb-8 mb-8'>
        <h2 className = 'mt-16 font-bold text-2xl'>Comentarios</h2>
        <Separator />
        <ListaComentsUser comments = {comments} />
    </div>
}